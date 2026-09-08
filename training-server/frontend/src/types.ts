export type ViewKey = 'shipment' | 'inbound' | 'inventory';

export type ShipmentStatus = 'CREATED' | 'RESERVED' | 'SHIPPED' | 'CANCELLED';

export interface InventoryBalance {
  availableQuantity: number;
  reservedQuantity: number;
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
