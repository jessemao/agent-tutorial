import { useMemo, useState } from 'react';
import { message } from 'antd';
import {
  cancelShipment,
  createIdentifiers,
  prepareDemoShipment,
  refreshShipment,
  SCENARIO,
} from '../api/wms';
import type { ScenarioIdentifiers, TrainingShipment } from '../types';

export function useTrainingScenario() {
  const [shipments, setShipments] = useState<TrainingShipment[]>([]);
  const [selectedId, setSelectedId] = useState<number>();
  const [pendingIdentifiers, setPendingIdentifiers] = useState<ScenarioIdentifiers>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();
  const [messageApi, messageContext] = message.useMessage();

  const selectedShipment = useMemo(
    () => shipments.find((shipment) => shipment.shipmentId === selectedId),
    [selectedId, shipments],
  );

  function openCreate(): ScenarioIdentifiers {
    const identifiers = createIdentifiers();
    setPendingIdentifiers(identifiers);
    return identifiers;
  }

  async function createDemo(): Promise<boolean> {
    if (!pendingIdentifiers || busy) return false;
    return run(async () => {
      const shipment = await prepareDemoShipment(pendingIdentifiers);
      setShipments((current) => [shipment, ...current]);
      setSelectedId(shipment.shipmentId);
      setPendingIdentifiers(undefined);
      messageApi.success('演示出库单已创建并完成库存预占');
    });
  }

  async function cancelSelected(reason: string): Promise<boolean> {
    if (!selectedShipment || selectedShipment.status !== 'RESERVED' || busy) return false;
    return run(async () => {
      await cancelShipment(selectedShipment, reason.trim() || SCENARIO.defaultCancelReason);
      const refreshed = await refreshShipment(selectedShipment);
      replaceShipment(refreshed);
      messageApi.success('出库单已取消，请到库存明细核对结果');
    });
  }

  async function refreshSelected(): Promise<boolean> {
    if (!selectedShipment || busy) return false;
    return run(async () => {
      const refreshed = await refreshShipment(selectedShipment);
      replaceShipment(refreshed);
      messageApi.success('单据与库存数据已刷新');
    });
  }

  function replaceShipment(next: TrainingShipment): void {
    setShipments((current) =>
      current.map((shipment) => (shipment.shipmentId === next.shipmentId ? next : shipment)),
    );
  }

  async function run(action: () => Promise<void>): Promise<boolean> {
    setBusy(true);
    setError(undefined);
    try {
      await action();
      return true;
    } catch (caught) {
      const text = caught instanceof Error ? caught.message : '操作失败';
      setError(text);
      messageApi.error(text);
      return false;
    } finally {
      setBusy(false);
    }
  }

  return {
    busy,
    cancelSelected,
    createDemo,
    error,
    messageContext,
    openCreate,
    pendingIdentifiers,
    refreshSelected,
    selectedId,
    selectedShipment,
    selectShipment: setSelectedId,
    shipments,
  };
}
