package warehouse

import (
	"sync"
	"time"
)

// ===== 内存存储 =====
type Store struct {
	mu       sync.RWMutex
	inbound  map[int64]*InboundOrder
	checks   map[int64]*QualityCheck
	locations map[int64]*StorageLocation
	batches  map[int64]*BatchInventory
	counts   map[int64]*InventoryCount
	waves    map[int64]*PickWave
	outbound map[int64]*OutboundOrder
	alerts   map[int64]*ExceptionAlert
	nextID   int64
}

func NewStore() *Store {
	s := &Store{
		inbound:  make(map[int64]*InboundOrder),
		checks:   make(map[int64]*QualityCheck),
		locations: make(map[int64]*StorageLocation),
		batches:  make(map[int64]*BatchInventory),
		counts:   make(map[int64]*InventoryCount),
		waves:    make(map[int64]*PickWave),
		outbound: make(map[int64]*OutboundOrder),
		alerts:   make(map[int64]*ExceptionAlert),
		nextID:   100,
	}
	s.initMockData()
	return s
}

func (s *Store) genID() int64 {
	s.nextID++
	return s.nextID - 1
}

func ptrTime(t time.Time) *time.Time { return &t }
func ptrInt(v int) *int              { return &v }

// ===== 初始化模拟数据 =====
func (s *Store) initMockData() {
	base := time.Date(2024, 6, 1, 9, 0, 0, 0, time.Local)

	// 库位
	locs := []*StorageLocation{
		{ID: 1, Code: "A-01-01", Zone: "A", Capacity: 100, Used: 60, Status: "OCCUPIED"},
		{ID: 2, Code: "A-01-02", Zone: "A", Capacity: 100, Used: 0, Status: "EMPTY"},
		{ID: 3, Code: "B-02-01", Zone: "B", Capacity: 80, Used: 45, Status: "OCCUPIED"},
		{ID: 4, Code: "B-02-02", Zone: "B", Capacity: 80, Used: 80, Status: "OCCUPIED"},
		{ID: 5, Code: "C-03-01", Zone: "C", Capacity: 50, Used: 0, Status: "EMPTY"},
	}
	for _, l := range locs {
		s.locations[l.ID] = l
	}

	// 入库预约
	loc1 := int64(1)
	s.inbound[1] = &InboundOrder{ID: 1, SKU: "SKU-001", Qty: 50, Supplier: "北京供应商", Status: "PENDING", CreatedAt: base}
	s.inbound[2] = &InboundOrder{ID: 2, SKU: "SKU-002", Qty: 30, Supplier: "上海供应商", Status: "CHECKED", BatchNo: "B20240601", LocationID: &loc1, CreatedAt: base.Add(24 * time.Hour), CheckedAt: ptrTime(base.Add(25 * time.Hour))}
	s.inbound[3] = &InboundOrder{ID: 3, SKU: "SKU-003", Qty: 20, Supplier: "广州供应商", Status: "SHELVED", BatchNo: "B20240602", LocationID: &loc1, CreatedAt: base.Add(48 * time.Hour), CheckedAt: ptrTime(base.Add(49 * time.Hour)), ShelvedAt: ptrTime(base.Add(50 * time.Hour))}

	// 质检
	s.checks[1] = &QualityCheck{ID: 1, OrderID: 2, Result: "PASSED", Checker: "质检员A", CheckedAt: ptrTime(base.Add(25 * time.Hour))}
	s.checks[2] = &QualityCheck{ID: 2, OrderID: 3, Result: "PASSED", Checker: "质检员B", CheckedAt: ptrTime(base.Add(49 * time.Hour))}

	// 批次库存
	s.batches[1] = &BatchInventory{ID: 1, BatchNo: "B20240602", SKU: "SKU-003", LocationID: 1, Qty: 20, InboundAt: base.Add(50 * time.Hour)}
	s.batches[2] = &BatchInventory{ID: 2, BatchNo: "B20240501", SKU: "SKU-001", LocationID: 3, Qty: 45, InboundAt: base.Add(-72 * time.Hour)}
	s.batches[3] = &BatchInventory{ID: 3, BatchNo: "B20240502", SKU: "SKU-004", LocationID: 4, Qty: 80, InboundAt: base.Add(-48 * time.Hour)}

	// 盘点单
	s.counts[1] = &InventoryCount{ID: 1, LocationID: 1, Status: "PLANNED", SystemQty: 60, CreatedAt: base.Add(72 * time.Hour)}
	s.counts[2] = &InventoryCount{ID: 2, LocationID: 3, Status: "COUNTING", SystemQty: 45, CreatedAt: base.Add(73 * time.Hour)}
	s.counts[3] = &InventoryCount{ID: 3, LocationID: 4, Status: "DIFF_FOUND", SystemQty: 80, RealQty: ptrInt(78), Diff: ptrInt(-2), Note: "短少2件", CreatedAt: base.Add(74 * time.Hour), CountedAt: ptrTime(base.Add(75 * time.Hour))}

	// 波次拣货
	s.waves[1] = &PickWave{ID: 1, WaveNo: "W20240601", Status: "CREATED", OrderIDs: []int64{1}, CreatedAt: base.Add(96 * time.Hour)}
	s.waves[2] = &PickWave{ID: 2, WaveNo: "W20240602", Status: "PICKING", OrderIDs: []int64{2}, CreatedAt: base.Add(97 * time.Hour)}

	// 出库单
	s.outbound[1] = &OutboundOrder{ID: 1, OrderNo: "OUT-001", SKU: "SKU-001", Qty: 10, Status: "PENDING", CreatedAt: base.Add(96 * time.Hour)}
	s.outbound[2] = &OutboundOrder{ID: 2, OrderNo: "OUT-002", SKU: "SKU-004", Qty: 5, Status: "REVIEWING", WaveID: ptrInt64(2), CreatedAt: base.Add(97 * time.Hour)}

	// 异常预警
	s.alerts[1] = &ExceptionAlert{ID: 1, Type: "STOCKOUT", Content: "SKU-002 库存不足", Status: "ACTIVE", CreatedAt: base.Add(100 * time.Hour)}
	s.alerts[2] = &ExceptionAlert{ID: 2, Type: "OVERSTOCK", Content: "库位 B-02-02 已满", Status: "ACKNOWLEDGED", RelatedID: 4, CreatedAt: base.Add(101 * time.Hour), AckAt: ptrTime(base.Add(102 * time.Hour))}
	s.alerts[3] = &ExceptionAlert{ID: 3, Type: "QUALITY", Content: "批次 B20240502 质检即将过期", Status: "RESOLVED", RelatedID: 3, CreatedAt: base.Add(103 * time.Hour), AckAt: ptrTime(base.Add(104 * time.Hour)), ResolveAt: ptrTime(base.Add(105 * time.Hour)), Note: "已重新质检"}
}

func ptrInt64(v int64) *int64 { return &v }
