"""仓储管理 WMS Flask 服务入口"""

from dataclasses import asdict
from datetime import datetime
from flask import Flask, jsonify, request
from flask_cors import CORS

from .models import InboundOrder, QualityCheck, BatchInventory, ExceptionAlert
from .store import Store

app = Flask(__name__)
CORS(app)

store = Store()


def ts():
    return datetime.now().strftime("%Y-%m-%dT%H:%M:%S")


def fail(msg):
    return jsonify({"ok": False, "msg": msg}), 400


# ===== 看板 =====
@app.get("/wms/dashboard")
def dashboard():
    s = store
    inbound_by_status = {}
    for o in s.inbound.values():
        inbound_by_status[o.status] = inbound_by_status.get(o.status, 0) + 1
    loc_by_status = {}
    for l in s.locations.values():
        loc_by_status[l.status] = loc_by_status.get(l.status, 0) + 1
    alert_by_status = {}
    for a in s.alerts.values():
        alert_by_status[a.status] = alert_by_status.get(a.status, 0) + 1
    total_batch_qty = sum(b.qty for b in s.batches.values())
    return jsonify({
        "totalInbound": len(s.inbound),
        "totalChecks": len(s.checks),
        "totalLocations": len(s.locations),
        "totalBatches": len(s.batches),
        "totalBatchQty": total_batch_qty,
        "totalCounts": len(s.counts),
        "totalWaves": len(s.waves),
        "totalOutbound": len(s.outbound),
        "totalAlerts": len(s.alerts),
        "inboundByStatus": inbound_by_status,
        "locByStatus": loc_by_status,
        "alertByStatus": alert_by_status,
    })


# ===== 入库预约 =====
@app.get("/wms/inbound")
def list_inbound():
    return jsonify([asdict(o) for o in store.inbound.values()])


@app.post("/wms/inbound")
def create_inbound():
    d = request.get_json(force=True)
    if not d.get("sku") or not d.get("qty") or not d.get("supplier"):
        return fail("SKU、数量和供应商不能为空")
    if int(d["qty"]) <= 0:
        return fail("数量必须大于0")
    oid = store.gen_id()
    o = InboundOrder(id=oid, sku=d["sku"], qty=int(d["qty"]),
                     supplier=d["supplier"], status="PENDING", created_at=ts())
    store.inbound[oid] = o
    return jsonify(asdict(o))


@app.post("/wms/inbound/<int:oid>/check")
def check_inbound(oid):
    d = request.get_json(force=True)
    result = d.get("result", "")
    checker = d.get("checker", "")
    if result not in ("PASSED", "FAILED"):
        return fail("质检结果必须为 PASSED 或 FAILED")
    if not checker:
        return fail("质检员不能为空")
    o = store.inbound.get(oid)
    if not o:
        return fail("入库单不存在")
    if o.status != "PENDING":
        return fail("仅待处理状态可质检")
    now = ts()
    o.status = "CHECKED"
    o.checked_at = now
    if result == "PASSED":
        o.batch_no = f"B{datetime.now().strftime('%Y%m%d')}-{oid}"
    cid = store.gen_id()
    store.checks[cid] = QualityCheck(
        id=cid, order_id=oid, result=result, note=d.get("note", ""),
        checker=checker, checked_at=now
    )
    return jsonify(asdict(o))


@app.post("/wms/inbound/<int:oid>/shelf")
def shelf_inbound(oid):
    d = request.get_json(force=True)
    loc_id = d.get("locationId")
    if not loc_id:
        return fail("请指定库位")
    loc_id = int(loc_id)
    o = store.inbound.get(oid)
    if not o:
        return fail("入库单不存在")
    if o.status != "CHECKED":
        return fail("仅已质检状态可上架")
    loc = store.locations.get(loc_id)
    if not loc:
        return fail("库位不存在")
    if loc.used + o.qty > loc.capacity:
        return fail("库位容量不足")
    now = ts()
    o.status = "SHELVED"
    o.shelved_at = now
    o.location_id = loc_id
    loc.used += o.qty
    loc.status = "OCCUPIED"
    bid = store.gen_id()
    store.batches[bid] = BatchInventory(
        id=bid, batch_no=o.batch_no, sku=o.sku, location_id=loc_id,
        qty=o.qty, inbound_at=now
    )
    return jsonify(asdict(o))


# ===== 库位 =====
@app.get("/wms/locations")
def list_locations():
    return jsonify([asdict(l) for l in store.locations.values()])


# ===== 批次库存 =====
@app.get("/wms/batches")
def list_batches():
    return jsonify([asdict(b) for b in store.batches.values()])


# ===== 盘点 =====
@app.get("/wms/counts")
def list_counts():
    return jsonify([asdict(c) for c in store.counts.values()])


@app.post("/wms/counts/<int:cid>/execute")
def execute_count(cid):
    d = request.get_json(force=True)
    real_qty = d.get("realQty")
    if real_qty is None:
        return fail("实际数量不能为空")
    real_qty = int(real_qty)
    c = store.counts.get(cid)
    if not c:
        return fail("盘点单不存在")
    if c.status not in ("PLANNED", "COUNTING"):
        return fail("仅计划或盘点中状态可执行")
    now = ts()
    c.real_qty = real_qty
    c.diff = real_qty - c.system_qty
    c.note = d.get("note", "")
    c.counted_at = now
    c.status = "DIFF_NONE" if c.diff == 0 else "DIFF_FOUND"
    if c.diff != 0:
        aid = store.gen_id()
        store.alerts[aid] = ExceptionAlert(
            id=aid, type="STOCKOUT",
            content=f"盘点差异: 库位{c.location_id} 差异{c.diff}",
            status="ACTIVE", related_id=c.id, created_at=now
        )
    return jsonify(asdict(c))


# ===== 波次拣货 =====
@app.get("/wms/pick-waves")
def list_waves():
    return jsonify([asdict(w) for w in store.waves.values()])


@app.post("/wms/pick-waves/<int:wid>/start")
def start_picking(wid):
    w = store.waves.get(wid)
    if not w:
        return fail("波次不存在")
    if w.status != "CREATED":
        return fail("仅已创建状态可开始拣货")
    w.status = "PICKING"
    return jsonify(asdict(w))


@app.post("/wms/pick-waves/<int:wid>/complete")
def complete_picking(wid):
    w = store.waves.get(wid)
    if not w:
        return fail("波次不存在")
    if w.status != "PICKING":
        return fail("仅拣货中状态可完成")
    now = ts()
    w.status = "PICKED"
    w.picked_at = now
    return jsonify(asdict(w))


# ===== 出库 =====
@app.get("/wms/outbound")
def list_outbound():
    return jsonify([asdict(o) for o in store.outbound.values()])


@app.post("/wms/outbound/<int:oid>/review")
def review_outbound(oid):
    o = store.outbound.get(oid)
    if not o:
        return fail("出库单不存在")
    if o.status != "PENDING":
        return fail("仅待处理状态可复核")
    now = ts()
    o.status = "REVIEWING"
    o.reviewed_at = now
    return jsonify(asdict(o))


@app.post("/wms/outbound/<int:oid>/complete")
def complete_outbound(oid):
    o = store.outbound.get(oid)
    if not o:
        return fail("出库单不存在")
    if o.status != "REVIEWING":
        return fail("仅复核中状态可完成出库")
    now = ts()
    o.status = "COMPLETED"
    o.reviewed_at = now
    for b in store.batches.values():
        if b.sku == o.sku and b.qty >= o.qty:
            b.qty -= o.qty
            loc = store.locations.get(b.location_id)
            if loc:
                loc.used = max(0, loc.used - o.qty)
                if loc.used == 0:
                    loc.status = "EMPTY"
            break
    return jsonify(asdict(o))


# ===== 异常预警 =====
@app.get("/wms/alerts")
def list_alerts():
    return jsonify([asdict(a) for a in store.alerts.values()])


@app.post("/wms/alerts/<int:aid>/ack")
def ack_alert(aid):
    a = store.alerts.get(aid)
    if not a:
        return fail("预警不存在")
    if a.status != "ACTIVE":
        return fail("仅活跃状态可确认")
    now = ts()
    a.status = "ACKNOWLEDGED"
    a.ack_at = now
    return jsonify(asdict(a))


@app.post("/wms/alerts/<int:aid>/resolve")
def resolve_alert(aid):
    d = request.get_json(force=True)
    note = d.get("note", "")
    if not note:
        return fail("请填写处理说明")
    a = store.alerts.get(aid)
    if not a:
        return fail("预警不存在")
    if a.status == "RESOLVED":
        return fail("已关闭的预警不可重复处理")
    now = ts()
    a.status = "RESOLVED"
    a.resolve_at = now
    a.note = note
    return jsonify(asdict(a))


# ===== 独立运行支持 =====
if __name__ == "__main__":
    # 直接运行时无需相对导入
    print("WMS 服务启动于 http://localhost:8081")
    app.run(port=8081, debug=True)
