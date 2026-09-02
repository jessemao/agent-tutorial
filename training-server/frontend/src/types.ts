export type ViewKey = 'shipment' | 'inventory';

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
