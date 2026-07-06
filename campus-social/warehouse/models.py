"""仓储管理 WMS 数据模型"""

from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional


@dataclass
class InboundOrder:
    id: int
    sku: str
    qty: int
    supplier: str
    status: str = "PENDING"  # PENDING -> CHECKED -> SHELVED
    batch_no: Optional[str] = None
    location_id: Optional[int] = None
    created_at: Optional[str] = None
    checked_at: Optional[str] = None
    shelved_at: Optional[str] = None


@dataclass
class QualityCheck:
    id: int
    order_id: int
    result: str = "PENDING"  # PENDING, PASSED, FAILED
    note: str = ""
    checker: str = ""
    checked_at: Optional[str] = None


@dataclass
class StorageLocation:
    id: int
    code: str
    zone: str
    capacity: int
    used: int = 0
    status: str = "EMPTY"  # EMPTY, OCCUPIED


@dataclass
class BatchInventory:
    id: int
    batch_no: str
    sku: str
    location_id: int
    qty: int
    inbound_at: Optional[str] = None


@dataclass
class InventoryCount:
    id: int
    location_id: int
    status: str = "PLANNED"  # PLANNED, COUNTING, DIFF_FOUND, DIFF_NONE
    system_qty: int = 0
    real_qty: Optional[int] = None
    diff: Optional[int] = None
    note: str = ""
    created_at: Optional[str] = None
    counted_at: Optional[str] = None


@dataclass
class PickWave:
    id: int
    wave_no: str
    status: str = "CREATED"  # CREATED, PICKING, PICKED
    order_ids: list = field(default_factory=list)
    created_at: Optional[str] = None
    picked_at: Optional[str] = None


@dataclass
class OutboundOrder:
    id: int
    order_no: str
    sku: str
    qty: int
    status: str = "PENDING"  # PENDING, REVIEWING, COMPLETED
    wave_id: Optional[int] = None
    created_at: Optional[str] = None
    reviewed_at: Optional[str] = None


@dataclass
class ExceptionAlert:
    id: int
    type: str  # STOCKOUT, OVERSTOCK, QUALITY, LOCATION_CONFLICT
    content: str
    status: str = "ACTIVE"  # ACTIVE, ACKNOWLEDGED, RESOLVED
    related_id: Optional[int] = None
    created_at: Optional[str] = None
    ack_at: Optional[str] = None
    resolve_at: Optional[str] = None
    note: str = ""
