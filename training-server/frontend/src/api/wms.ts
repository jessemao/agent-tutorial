import type {
  InventoryBalance,
  InboundView,
  ScenarioIdentifiers,
  ShipmentView,
  TrainingInbound,
  TrainingShipment,
} from '../types';

export const SCENARIO = Object.freeze({
  initialQuantity: 10,
  shipmentQuantity: 6,
  warehouseId: 1,
  locationId: 1,
  operator: 'trainer',
  defaultCancelReason: '客户取消',
});

export const T02_SCENARIO = Object.freeze({
  plannedQuantity: 10,
  firstReceiptQuantity: 4,
  finalReceiptQuantity: 6,
  warehouseId: 1,
  locationId: 1,
  operator: 'trainer',
});

interface ApiResponse<T> {
  success: boolean;
  code?: string | number;
  message?: string;
  data: T;
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'X-Operator': SCENARIO.operator,
  };
  const options: RequestInit = { method, headers };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  let response: Response;
  try {
    response = await fetch(path, options);
  } catch {
    throw new Error('无法连接服务，请确认 Docker 服务已启动。');
  }

  let payload: ApiResponse<T>;
  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    throw new Error(`HTTP ${response.status}: 服务响应不是有效 JSON`);
  }

  if (!response.ok || !payload.success) {
    throw new Error(`${payload.code ?? response.status}: ${payload.message ?? '请求失败'}`);
  }
  return payload.data;
}

function inventoryPath(shipment: Pick<TrainingShipment, 'skuId'>): string {
  const query = new URLSearchParams({
    skuId: String(shipment.skuId),
    warehouseId: String(SCENARIO.warehouseId),
    locationId: String(SCENARIO.locationId),
  });
  return `/api/wms/inventory/balance?${query.toString()}`;
}

export function queryInventoryBalance(skuId: number, locationId: number = SCENARIO.locationId): Promise<InventoryBalance> {
  const query = new URLSearchParams({
    skuId: String(skuId),
    warehouseId: String(SCENARIO.warehouseId),
    locationId: String(locationId),
  });
  return request<InventoryBalance>('GET', `/api/wms/inventory/balance?${query.toString()}`);
}

export function createIdentifiers(): ScenarioIdentifiers {
  const values = crypto.getRandomValues(new Uint32Array(3));
  const runId = Array.from(values, (value) => value.toString(16).padStart(8, '0')).join('');
  const skuId = 1_000_000_000_000 + values[0] * 100_000 + (values[1] % 100_000);
  return { runId, skuId, orderNo: `SO-T01-${runId.slice(0, 12).toUpperCase()}` };
}

export function createInboundIdentifiers(): ScenarioIdentifiers {
  const values = crypto.getRandomValues(new Uint32Array(3));
  const runId = Array.from(values, (value) => value.toString(16).padStart(8, '0')).join('');
  const skuId = 2_000_000_000_000 + values[0] * 100_000 + (values[1] % 100_000);
  return { runId, skuId, orderNo: `IN-T02-${runId.slice(0, 12).toUpperCase()}` };
}

export async function createInboundOrder(identifiers: ScenarioIdentifiers): Promise<TrainingInbound> {
  const created = await request<InboundView>('POST', '/api/wms/inbounds', {
    orderNo: identifiers.orderNo,
    skuId: identifiers.skuId,
    warehouseId: T02_SCENARIO.warehouseId,
    locationId: T02_SCENARIO.locationId,
    plannedQuantity: T02_SCENARIO.plannedQuantity,
  });
  return {
    ...identifiers,
    inboundId: created.id,
    plannedQuantity: created.plannedQuantity,
    receivedQuantity: created.receivedQuantity,
    status: created.status,
    inventory: { availableQuantity: 0, reservedQuantity: 0 },
    createdAt: new Date(),
  };
}

export async function receiveInbound(inbound: TrainingInbound, quantity: number): Promise<void> {
  await request<InboundView>('POST', `/api/wms/inbounds/${inbound.inboundId}/receive`, {
    idempotencyKey: `receive-t02-${inbound.runId}-${inbound.receivedQuantity}`,
    quantity,
  });
}

export async function refreshInbound(inbound: TrainingInbound): Promise<TrainingInbound> {
  const [view, inventory] = await Promise.all([
    request<InboundView>('GET', `/api/wms/inbounds/${inbound.inboundId}`),
    request<InventoryBalance>('GET', inventoryPath(inbound)),
  ]);
  return {
    ...inbound,
    plannedQuantity: view.plannedQuantity,
    receivedQuantity: view.receivedQuantity,
    status: view.status,
    inventory,
  };
}

export async function prepareDemoShipment(
  identifiers: ScenarioIdentifiers,
): Promise<TrainingShipment> {
  await request('POST', '/api/wms/inventory/receive', {
    idempotencyKey: `receive-t01-${identifiers.runId}`,
    referenceNo: `RC-T01-${identifiers.runId}`,
    skuId: identifiers.skuId,
    warehouseId: SCENARIO.warehouseId,
    locationId: SCENARIO.locationId,
    quantity: SCENARIO.initialQuantity,
  });

  const created = await request<ShipmentView>('POST', '/api/wms/shipments', {
    orderNo: identifiers.orderNo,
    skuId: identifiers.skuId,
    warehouseId: SCENARIO.warehouseId,
    locationId: SCENARIO.locationId,
    quantity: SCENARIO.shipmentQuantity,
  });
  const reserved = await request<ShipmentView>(
    'POST',
    `/api/wms/shipments/${created.id}/reserve`,
    { idempotencyKey: `reserve-t01-${identifiers.runId}` },
  );
  const inventory = await request<InventoryBalance>('GET', inventoryPath(identifiers));

  return {
    ...identifiers,
    shipmentId: reserved.id,
    status: reserved.status,
    cancelReason: reserved.cancelReason,
    quantity: SCENARIO.shipmentQuantity,
    createdAt: new Date(),
    inventory,
  };
}

export async function cancelShipment(shipment: TrainingShipment, reason: string): Promise<void> {
  await request('POST', `/api/wms/shipments/${shipment.shipmentId}/cancel`, {
    idempotencyKey: `cancel-t01-${shipment.runId}`,
    reason,
  });
}

export async function refreshShipment(shipment: TrainingShipment): Promise<TrainingShipment> {
  const [view, inventory] = await Promise.all([
    request<ShipmentView>('GET', `/api/wms/shipments/${shipment.shipmentId}`),
    request<InventoryBalance>('GET', inventoryPath(shipment)),
  ]);
  return {
    ...shipment,
    status: view.status,
    cancelReason: view.cancelReason,
    inventory,
  };
}
