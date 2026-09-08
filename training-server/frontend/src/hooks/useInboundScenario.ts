import { useState } from 'react';
import { message } from 'antd';
import {
  createInboundIdentifiers,
  createInboundOrder,
  receiveInbound,
  refreshInbound,
  T02_SCENARIO,
} from '../api/wms';
import type { ScenarioIdentifiers, TrainingInbound } from '../types';

export function useInboundScenario() {
  const [inbounds, setInbounds] = useState<TrainingInbound[]>([]);
  const [selectedId, setSelectedId] = useState<number>();
  const [pendingIdentifiers, setPendingIdentifiers] = useState<ScenarioIdentifiers>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();
  const [messageApi, messageContext] = message.useMessage();

  const selected = inbounds.find((inbound) => inbound.inboundId === selectedId);

  function openCreate(): ScenarioIdentifiers {
    const identifiers = createInboundIdentifiers();
    setPendingIdentifiers(identifiers);
    return identifiers;
  }

  async function createDemo(): Promise<boolean> {
    if (!pendingIdentifiers || busy) return false;
    setBusy(true);
    setError(undefined);
    try {
      const inbound = await createInboundOrder(pendingIdentifiers);
      setInbounds((current) => [inbound, ...current]);
      setSelectedId(inbound.inboundId);
      setPendingIdentifiers(undefined);
      messageApi.success('入库单已创建，等待收货');
      return true;
    } catch (caught) {
      return fail(caught);
    } finally {
      setBusy(false);
    }
  }

  async function receiveNextBatch(): Promise<boolean> {
    if (!selected || busy) return false;
    setBusy(true);
    setError(undefined);
    let receiptError: unknown;
    try {
      const quantity = selected.status === 'PARTIALLY_RECEIVED'
        ? T02_SCENARIO.finalReceiptQuantity
        : T02_SCENARIO.firstReceiptQuantity;
      await receiveInbound(selected, quantity);
    } catch (caught) {
      receiptError = caught;
    }
    try {
      replace(await refreshInbound(selected));
    } catch (caught) {
      return fail(caught);
    } finally {
      setBusy(false);
    }
    return receiptError === undefined ? true : fail(receiptError);
  }

  async function refreshSelected(): Promise<boolean> {
    if (!selected || busy) return false;
    setBusy(true);
    try {
      replace(await refreshInbound(selected));
      return true;
    } catch (caught) {
      return fail(caught);
    } finally {
      setBusy(false);
    }
  }

  function replace(next: TrainingInbound): void {
    setInbounds((current) => current.map((item) => (item.inboundId === next.inboundId ? next : item)));
  }

  function fail(caught: unknown): false {
    const text = caught instanceof Error ? caught.message : '操作失败';
    setError(text);
    messageApi.error(text);
    return false;
  }

  return {
    busy,
    createDemo,
    error,
    inbounds,
    messageContext,
    openCreate,
    pendingIdentifiers,
    receiveNextBatch,
    refreshSelected,
    selected,
    selectedId,
    selectInbound: setSelectedId,
  };
}
