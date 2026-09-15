export type ViewKey = 'shipment' | 'inbound' | 'inventory' | 'transfer' | 'count';

export interface MasterDataOption { id: number; code: string; name: string; }
export interface InventoryCountLineView { sku: MasterDataOption; location: MasterDataOption; creationBookTotal: number; countedTotal?: number | null; submittedBookTotal?: number | null; differenceReason?: string | null; approvalBookTotal?: number | null; difference?: number | null; availableAfter?: number | null; reservedAfter?: number | null; }
export interface InventoryCountView { id: number; countNo: string; status: 'DRAFT' | 'SUBMITTED' | 'REJECTED' | 'APPROVED' | 'CANCELLED'; version: number; warehouse: MasterDataOption; lines: InventoryCountLineView[]; createdBy: string; createdAt: string; note?: string | null; submittedBy?: string | null; submittedAt?: string | null; rejectedBy?: string | null; rejectedAt?: string | null; rejectionReason?: string | null; cancelledBy?: string | null; cancelledAt?: string | null; cancellationReason?: string | null; correctionOfCountNo?: string | null; approvedBy?: string | null; approvedAt?: string | null; lastApprovalFailureCode?: string | null; lastApprovalFailureAt?: string | null; }
export interface InventoryCountPage { items: InventoryCountView[]; total: number; page: number; size: number; }

export type ShipmentStatus = 'CREATED' | 'RESERVED' | 'SHIPPED' | 'CANCELLED';

export interface InventoryBalance {
  availableQuantity: number;
  reservedQuantity: number;
}

export interface InventoryTransferView {
  transferNo: string;
  source: InventoryBalance;
  target: InventoryBalance;
}

export interface InventoryTransferTask {
  transferNo: string;
  skuId: number;
  warehouseId: number;
  sourceLocationId: number;
  targetLocationId: number;
  quantity: number;
  createdAt: string;
}

export interface ShipmentView {
  id: number;
  status: ShipmentStatus;
  cancelReason?: string | null;
}

export interface ScenarioIdentifiers {
  runId: string;
  skuId: number;
  orderNo: string;
}

export interface TrainingShipment extends ScenarioIdentifiers {
  shipmentId: number;
  status: ShipmentStatus;
  cancelReason?: string | null;
  quantity: number;
  createdAt: Date;
  inventory: InventoryBalance;
}

export type InboundStatus = 'CREATED' | 'PARTIALLY_RECEIVED' | 'RECEIVED';

export interface InboundView {
  id: number;
  orderNo: string;
  skuId: number;
  warehouseId: number;
  locationId: number;
  plannedQuantity: number;
  receivedQuantity: number;
  status: InboundStatus;
}

export interface TrainingInbound extends ScenarioIdentifiers {
  inboundId: number;
  plannedQuantity: number;
  receivedQuantity: number;
  status: InboundStatus;
  inventory: InventoryBalance;
  createdAt: Date;
}
