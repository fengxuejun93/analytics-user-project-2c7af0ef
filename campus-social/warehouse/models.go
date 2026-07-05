package warehouse

import "time"

// ===== 入库预约 =====
type InboundOrder struct {
	ID        int64      `json:"id"`
	SKU       string     `json:"sku"`
	Qty       int        `json:"qty"`
	Supplier  string     `json:"supplier"`
	Status    string     `json:"status"` // PENDING, CHECKED, SHELVED
	BatchNo   string     `json:"batchNo,omitempty"`
	LocationID *int64    `json:"locationId,omitempty"`
	CreatedAt time.Time  `json:"createdAt"`
	CheckedAt *time.Time `json:"checkedAt,omitempty"`
	ShelvedAt *time.Time `json:"shelvedAt,omitempty"`
}

// ===== 质检记录 =====
type QualityCheck struct {
	ID        int64      `json:"id"`
	OrderID   int64      `json:"orderId"`
	Result    string     `json:"result"` // PENDING, PASSED, FAILED
	Note      string     `json:"note,omitempty"`
	Checker   string     `json:"checker"`
	CheckedAt *time.Time `json:"checkedAt,omitempty"`
}

// ===== 库位 =====
type StorageLocation struct {
	ID       int64  `json:"id"`
	Code     string `json:"code"`
	Zone     string `json:"zone"`
	Capacity int    `json:"capacity"`
	Used     int    `json:"used"`
	Status   string `json:"status"` // EMPTY, OCCUPIED
}

// ===== 批次库存 =====
type BatchInventory struct {
	ID         int64     `json:"id"`
	BatchNo    string    `json:"batchNo"`
	SKU        string    `json:"sku"`
	LocationID int64     `json:"locationId"`
	Qty        int       `json:"qty"`
	InboundAt  time.Time `json:"inboundAt"`
}

// ===== 盘点单 =====
type InventoryCount struct {
	ID         int64      `json:"id"`
	LocationID int64      `json:"locationId"`
	Status     string     `json:"status"` // PLANNED, COUNTING, DIFF_FOUND, DIFF_NONE
	SystemQty  int        `json:"systemQty"`
	RealQty    *int       `json:"realQty,omitempty"`
	Diff       *int       `json:"diff,omitempty"`
	Note       string     `json:"note,omitempty"`
	CreatedAt  time.Time  `json:"createdAt"`
	CountedAt  *time.Time `json:"countedAt,omitempty"`
}

// ===== 波次拣货 =====
type PickWave struct {
	ID        int64      `json:"id"`
	WaveNo    string     `json:"waveNo"`
	Status    string     `json:"status"` // CREATED, PICKING, PICKED
	OrderIDs  []int64    `json:"orderIds"`
	CreatedAt time.Time  `json:"createdAt"`
	PickedAt  *time.Time `json:"pickedAt,omitempty"`
}

// ===== 出库单 =====
type OutboundOrder struct {
	ID         int64      `json:"id"`
	OrderNo    string     `json:"orderNo"`
	SKU        string     `json:"sku"`
	Qty        int        `json:"qty"`
	Status     string     `json:"status"` // PENDING, REVIEWING, COMPLETED
	WaveID     *int64     `json:"waveId,omitempty"`
	CreatedAt  time.Time  `json:"createdAt"`
	ReviewedAt *time.Time `json:"reviewedAt,omitempty"`
}

// ===== 异常预警 =====
type ExceptionAlert struct {
	ID         int64      `json:"id"`
	Type       string     `json:"type"` // STOCKOUT, OVERSTOCK, QUALITY, LOCATION_CONFLICT
	Content    string     `json:"content"`
	Status     string     `json:"status"` // ACTIVE, ACKNOWLEDGED, RESOLVED
	RelatedID  int64      `json:"relatedId,omitempty"`
	CreatedAt  time.Time  `json:"createdAt"`
	AckAt      *time.Time `json:"ackAt,omitempty"`
	ResolveAt  *time.Time `json:"resolveAt,omitempty"`
	Note       string     `json:"note,omitempty"`
}

// ===== API 通用响应 =====
type APIResult struct {
	OK   bool   `json:"ok"`
	Msg  string `json:"msg,omitempty"`
}
