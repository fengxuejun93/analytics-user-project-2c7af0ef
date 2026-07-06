"""仓储管理 WMS 内存存储 + 模拟数据"""

import threading
from datetime import datetime
from .models import (
    InboundOrder, QualityCheck, StorageLocation, BatchInventory,
    InventoryCount, PickWave, OutboundOrder, ExceptionAlert,
)


class Store:
    def __init__(self):
        self._lock = threading.Lock()
        self._next_id = 100
        self.inbound: dict[int, InboundOrder] = {}
        self.checks: dict[int, QualityCheck] = {}
        self.locations: dict[int, StorageLocation] = {}
        self.batches: dict[int, BatchInventory] = {}
        self.counts: dict[int, InventoryCount] = {}
        self.waves: dict[int, PickWave] = {}
        self.outbound: dict[int, OutboundOrder] = {}
        self.alerts: dict[int, ExceptionAlert] = {}
        self._init_mock()

    def gen_id(self) -> int:
        self._next_id += 1
        return self._next_id - 1

    # ---- Mock 数据 ----
    def _init_mock(self):
        base = datetime(2024, 6, 1, 9, 0, 0)
        ts = lambda dt: dt.strftime("%Y-%m-%dT%H:%M:%S")
        after = lambda h: ts(base.replace(hour=(base.hour + h) % 24) if h < 15 else base.replace(day=base.day + 1, hour=(base.hour + h - 24)))

        # 库位
        locs = [
            StorageLocation(1, "A-01-01", "A", 100, 60, "OCCUPIED"),
            StorageLocation(2, "A-01-02", "A", 100, 0, "EMPTY"),
            StorageLocation(3, "B-02-01", "B", 80, 45, "OCCUPIED"),
            StorageLocation(4, "B-02-02", "B", 80, 80, "OCCUPIED"),
            StorageLocation(5, "C-03-01", "C", 50, 0, "EMPTY"),
        ]
        for l in locs:
            self.locations[l.id] = l

        # 入库预约
        self.inbound[1] = InboundOrder(1, "SKU-001", 50, "北京供应商", "PENDING", created_at=ts(base))
        self.inbound[2] = InboundOrder(2, "SKU-002", 30, "上海供应商", "CHECKED", batch_no="B20240601-2", location_id=1, created_at=ts(base.replace(day=2)), checked_at=ts(base.replace(day=2, hour=10)))
        self.inbound[3] = InboundOrder(3, "SKU-003", 20, "广州供应商", "SHELVED", batch_no="B20240602-3", location_id=1, created_at=ts(base.replace(day=3)), checked_at=ts(base.replace(day=3, hour=10)), shelved_at=ts(base.replace(day=3, hour=11)))

        # 质检
        self.checks[1] = QualityCheck(1, 2, "PASSED", checker="质检员A", checked_at=ts(base.replace(day=2, hour=10)))
        self.checks[2] = QualityCheck(2, 3, "PASSED", checker="质检员B", checked_at=ts(base.replace(day=3, hour=10)))

        # 批次
        self.batches[1] = BatchInventory(1, "B20240602-3", "SKU-003", 1, 20, ts(base.replace(day=3, hour=11)))
        self.batches[2] = BatchInventory(2, "B20240501", "SKU-001", 3, 45, ts(base.replace(month=5, day=28)))
        self.batches[3] = BatchInventory(3, "B20240502", "SKU-004", 4, 80, ts(base.replace(month=5, day=29)))

        # 盘点
        self.counts[1] = InventoryCount(1, 1, "PLANNED", 60, created_at=ts(base.replace(day=4)))
        self.counts[2] = InventoryCount(2, 3, "COUNTING", 45, created_at=ts(base.replace(day=4, hour=10)))
        self.counts[3] = InventoryCount(3, 4, "DIFF_FOUND", 80, 78, -2, "短少2件", ts(base.replace(day=4, hour=12)), ts(base.replace(day=4, hour=13)))

        # 波次
        self.waves[1] = PickWave(1, "W20240601", "CREATED", [1], ts(base.replace(day=5)))
        self.waves[2] = PickWave(2, "W20240602", "PICKING", [2], ts(base.replace(day=5, hour=10)))

        # 出库
        self.outbound[1] = OutboundOrder(1, "OUT-001", "SKU-001", 10, "PENDING", created_at=ts(base.replace(day=5)))
        self.outbound[2] = OutboundOrder(2, "OUT-002", "SKU-004", 5, "REVIEWING", wave_id=2, created_at=ts(base.replace(day=5, hour=10)))

        # 预警
        self.alerts[1] = ExceptionAlert(1, "STOCKOUT", "SKU-002 库存不足", "ACTIVE", created_at=ts(base.replace(day=6)))
        self.alerts[2] = ExceptionAlert(2, "OVERSTOCK", "库位 B-02-02 已满", "ACKNOWLEDGED", related_id=4, created_at=ts(base.replace(day=6, hour=10)), ack_at=ts(base.replace(day=6, hour=11)))
        self.alerts[3] = ExceptionAlert(3, "QUALITY", "批次 B20240502 质检即将过期", "RESOLVED", related_id=3, created_at=ts(base.replace(day=6, hour=12)), ack_at=ts(base.replace(day=6, hour=13)), resolve_at=ts(base.replace(day=6, hour=14)), note="已重新质检")
