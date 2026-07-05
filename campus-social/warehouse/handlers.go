package warehouse

import (
	"encoding/json"
	"net/http"
	"strconv"
	"time"
)

type Handler struct {
	store *Store
}

func NewHandler(s *Store) *Handler {
	return &Handler{store: s}
}

func jsonResp(w http.ResponseWriter, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(data)
}

func (h *Handler) Dashboard(w http.ResponseWriter, r *http.Request) {
	s := h.store
	s.mu.RLock()
	defer s.mu.RUnlock()

	inboundByStatus := map[string]int{}
	for _, o := range s.inbound {
		inboundByStatus[o.Status]++
	}
	locByStatus := map[string]int{}
	for _, l := range s.locations {
		locByStatus[l.Status]++
	}
	alertByStatus := map[string]int{}
	for _, a := range s.alerts {
		alertByStatus[a.Status]++
	}

	totalBatchQty := 0
	for _, b := range s.batches {
		totalBatchQty += b.Qty
	}

	jsonResp(w, map[string]interface{}{
		"totalInbound":  len(s.inbound),
		"totalChecks":   len(s.checks),
		"totalLocations": len(s.locations),
		"totalBatches":  len(s.batches),
		"totalBatchQty": totalBatchQty,
		"totalCounts":   len(s.counts),
		"totalWaves":    len(s.waves),
		"totalOutbound": len(s.outbound),
		"totalAlerts":   len(s.alerts),
		"inboundByStatus": inboundByStatus,
		"locByStatus":    locByStatus,
		"alertByStatus":  alertByStatus,
	})
}

// ===== 入库预约 =====

func (h *Handler) ListInbound(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*InboundOrder, 0, len(h.store.inbound))
	for _, o := range h.store.inbound {
		list = append(list, o)
	}
	jsonResp(w, list)
}

func (h *Handler) CreateInbound(w http.ResponseWriter, r *http.Request) {
	var req InboundOrder
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		jsonResp(w, APIResult{false, "请求格式错误"})
		return
	}
	if req.SKU == "" || req.Qty <= 0 || req.Supplier == "" {
		jsonResp(w, APIResult{false, "SKU、数量和供应商不能为空"})
		return
	}
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	req.ID = h.store.genID()
	req.Status = "PENDING"
	req.CreatedAt = time.Now()
	h.store.inbound[req.ID] = &req
	jsonResp(w, req)
}

func (h *Handler) CheckInbound(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	var body struct {
		Result  string `json:"result"`
		Checker string `json:"checker"`
		Note    string `json:"note"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		jsonResp(w, APIResult{false, "请求格式错误"})
		return
	}
	if body.Result != "PASSED" && body.Result != "FAILED" {
		jsonResp(w, APIResult{false, "质检结果必须为 PASSED 或 FAILED"})
		return
	}
	if body.Checker == "" {
		jsonResp(w, APIResult{false, "质检员不能为空"})
		return
	}
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	o, ok := h.store.inbound[id]
	if !ok {
		jsonResp(w, APIResult{false, "入库单不存在"})
		return
	}
	if o.Status != "PENDING" {
		jsonResp(w, APIResult{false, "仅待处理状态可质检"})
		return
	}
	now := time.Now()
	o.Status = "CHECKED"
	o.CheckedAt = &now
	h.store.checks[h.store.genID()] = &QualityCheck{
		ID:        h.store.nextID - 1,
		OrderID:   id,
		Result:    body.Result,
		Checker:   body.Checker,
		Note:      body.Note,
		CheckedAt: &now,
	}
	if body.Result == "PASSED" {
		o.BatchNo = "B" + now.Format("20060102") + "-" + strconv.FormatInt(id, 10)
	}
	jsonResp(w, o)
}

func (h *Handler) ShelfInbound(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	var body struct {
		LocationID int64 `json:"locationId"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		jsonResp(w, APIResult{false, "请求格式错误"})
		return
	}
	if body.LocationID == 0 {
		jsonResp(w, APIResult{false, "请指定库位"})
		return
	}
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	o, ok := h.store.inbound[id]
	if !ok {
		jsonResp(w, APIResult{false, "入库单不存在"})
		return
	}
	if o.Status != "CHECKED" {
		jsonResp(w, APIResult{false, "仅已质检状态可上架"})
		return
	}
	loc, lok := h.store.locations[body.LocationID]
	if !lok {
		jsonResp(w, APIResult{false, "库位不存在"})
		return
	}
	if loc.Used+o.Qty > loc.Capacity {
		jsonResp(w, APIResult{false, "库位容量不足"})
		return
	}
	now := time.Now()
	o.Status = "SHELVED"
	o.ShelvedAt = &now
	o.LocationID = &body.LocationID
	loc.Used += o.Qty
	loc.Status = "OCCUPIED"
	h.store.batches[h.store.genID()] = &BatchInventory{
		ID:         h.store.nextID - 1,
		BatchNo:    o.BatchNo,
		SKU:        o.SKU,
		LocationID: body.LocationID,
		Qty:        o.Qty,
		InboundAt:  now,
	}
	jsonResp(w, o)
}

// ===== 库位 =====

func (h *Handler) ListLocations(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*StorageLocation, 0, len(h.store.locations))
	for _, l := range h.store.locations {
		list = append(list, l)
	}
	jsonResp(w, list)
}

// ===== 批次库存 =====

func (h *Handler) ListBatches(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*BatchInventory, 0, len(h.store.batches))
	for _, b := range h.store.batches {
		list = append(list, b)
	}
	jsonResp(w, list)
}

// ===== 盘点 =====

func (h *Handler) ListCounts(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*InventoryCount, 0, len(h.store.counts))
	for _, c := range h.store.counts {
		list = append(list, c)
	}
	jsonResp(w, list)
}

func (h *Handler) ExecuteCount(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	var body struct {
		RealQty int    `json:"realQty"`
		Note    string `json:"note"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		jsonResp(w, APIResult{false, "请求格式错误"})
		return
	}
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	c, ok := h.store.counts[id]
	if !ok {
		jsonResp(w, APIResult{false, "盘点单不存在"})
		return
	}
	if c.Status != "PLANNED" && c.Status != "COUNTING" {
		jsonResp(w, APIResult{false, "仅计划或盘点中状态可执行"})
		return
	}
	now := time.Now()
	c.RealQty = &body.RealQty
	c.Diff = ptrInt(body.RealQty - c.SystemQty)
	c.Note = body.Note
	c.CountedAt = &now
	if *c.Diff == 0 {
		c.Status = "DIFF_NONE"
	} else {
		c.Status = "DIFF_FOUND"
	}
	// 差异时生成异常预警
	if *c.Diff != 0 {
		h.store.alerts[h.store.genID()] = &ExceptionAlert{
			ID:        h.store.nextID - 1,
			Type:      "STOCKOUT",
			Content:   "盘点差异: 库位" + strconv.FormatInt(c.LocationID, 10) + " 差异" + strconv.Itoa(*c.Diff),
			Status:    "ACTIVE",
			RelatedID: c.ID,
			CreatedAt: now,
		}
	}
	jsonResp(w, c)
}

// ===== 波次拣货 =====

func (h *Handler) ListWaves(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*PickWave, 0, len(h.store.waves))
	for _, w := range h.store.waves {
		list = append(list, w)
	}
	jsonResp(w, list)
}

func (h *Handler) StartPicking(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	w2, ok := h.store.waves[id]
	if !ok {
		jsonResp(w, APIResult{false, "波次不存在"})
		return
	}
	if w2.Status != "CREATED" {
		jsonResp(w, APIResult{false, "仅已创建状态可开始拣货"})
		return
	}
	w2.Status = "PICKING"
	jsonResp(w, w2)
}

func (h *Handler) CompletePicking(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	w2, ok := h.store.waves[id]
	if !ok {
		jsonResp(w, APIResult{false, "波次不存在"})
		return
	}
	if w2.Status != "PICKING" {
		jsonResp(w, APIResult{false, "仅拣货中状态可完成"})
		return
	}
	now := time.Now()
	w2.Status = "PICKED"
	w2.PickedAt = &now
	jsonResp(w, w2)
}

// ===== 出库 =====

func (h *Handler) ListOutbound(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*OutboundOrder, 0, len(h.store.outbound))
	for _, o := range h.store.outbound {
		list = append(list, o)
	}
	jsonResp(w, list)
}

func (h *Handler) ReviewOutbound(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	o, ok := h.store.outbound[id]
	if !ok {
		jsonResp(w, APIResult{false, "出库单不存在"})
		return
	}
	if o.Status != "PENDING" {
		jsonResp(w, APIResult{false, "仅待处理状态可复核"})
		return
	}
	o.Status = "REVIEWING"
	now := time.Now()
	o.ReviewedAt = &now
	jsonResp(w, o)
}

func (h *Handler) CompleteOutbound(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	o, ok := h.store.outbound[id]
	if !ok {
		jsonResp(w, APIResult{false, "出库单不存在"})
		return
	}
	if o.Status != "REVIEWING" {
		jsonResp(w, APIResult{false, "仅复核中状态可完成出库"})
		return
	}
	o.Status = "COMPLETED"
	now := time.Now()
	o.ReviewedAt = &now
	// 扣减批次库存
	for _, b := range h.store.batches {
		if b.SKU == o.SKU && b.Qty >= o.Qty {
			b.Qty -= o.Qty
			// 更新库位
			if loc, ok := h.store.locations[b.LocationID]; ok {
				loc.Used -= o.Qty
				if loc.Used <= 0 {
					loc.Used = 0
					loc.Status = "EMPTY"
				}
			}
			break
		}
	}
	jsonResp(w, o)
}

// ===== 异常预警 =====

func (h *Handler) ListAlerts(w http.ResponseWriter, r *http.Request) {
	h.store.mu.RLock()
	defer h.store.mu.RUnlock()
	list := make([]*ExceptionAlert, 0, len(h.store.alerts))
	for _, a := range h.store.alerts {
		list = append(list, a)
	}
	jsonResp(w, list)
}

func (h *Handler) AckAlert(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	a, ok := h.store.alerts[id]
	if !ok {
		jsonResp(w, APIResult{false, "预警不存在"})
		return
	}
	if a.Status != "ACTIVE" {
		jsonResp(w, APIResult{false, "仅活跃状态可确认"})
		return
	}
	now := time.Now()
	a.Status = "ACKNOWLEDGED"
	a.AckAt = &now
	jsonResp(w, a)
}

func (h *Handler) ResolveAlert(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.ParseInt(r.PathValue("id"), 10, 64)
	var body struct {
		Note string `json:"note"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		jsonResp(w, APIResult{false, "请求格式错误"})
		return
	}
	if body.Note == "" {
		jsonResp(w, APIResult{false, "请填写处理说明"})
		return
	}
	h.store.mu.Lock()
	defer h.store.mu.Unlock()
	a, ok := h.store.alerts[id]
	if !ok {
		jsonResp(w, APIResult{false, "预警不存在"})
		return
	}
	if a.Status == "RESOLVED" {
		jsonResp(w, APIResult{false, "已关闭的预警不可重复处理"})
		return
	}
	now := time.Now()
	a.Status = "RESOLVED"
	a.ResolveAt = &now
	a.Note = body.Note
	jsonResp(w, a)
}
