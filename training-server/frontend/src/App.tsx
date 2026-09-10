import { useState } from 'react';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { AppShell } from './components/AppShell';
import { useTrainingScenario } from './hooks/useTrainingScenario';
import { useInboundScenario } from './hooks/useInboundScenario';
import { InboundPage } from './pages/InboundPage';
import { InventoryPage } from './pages/InventoryPage';
import { ShipmentPage } from './pages/ShipmentPage';
import { TransferPage } from './pages/TransferPage';
import type { ViewKey } from './types';

export default function App() {
  const [activeView, setActiveView] = useState<ViewKey>('shipment');
  const scenario = useTrainingScenario();
  const inbound = useInboundScenario();

  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#2563eb',
          borderRadius: 4,
          colorBgLayout: '#f3f4f6',
          fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', sans-serif",
        },
        components: {
          Layout: { siderBg: '#0d1726', headerBg: '#ffffff' },
          Menu: { darkItemBg: '#0d1726', darkSubMenuItemBg: '#111d2e', darkItemSelectedBg: '#2563eb' },
          Table: { headerBg: '#f8fafc', headerColor: '#334155' },
        },
      }}
    >
      {scenario.messageContext}
      {inbound.messageContext}
      <AppShell activeView={activeView} onViewChange={setActiveView}>
        {activeView === 'shipment' && (
          <ShipmentPage
            busy={scenario.busy}
            error={scenario.error}
            pendingIdentifiers={scenario.pendingIdentifiers}
            selectedId={scenario.selectedId}
            shipments={scenario.shipments}
            onCancel={scenario.cancelSelected}
            onCreate={scenario.createDemo}
            onInventory={() => setActiveView('inventory')}
            onOpenCreate={scenario.openCreate}
            onRefresh={scenario.refreshSelected}
            onSelect={scenario.selectShipment}
          />
        )}
        {activeView === 'inbound' && (
          <InboundPage
            busy={inbound.busy}
            error={inbound.error}
            inbounds={inbound.inbounds}
            pendingIdentifiers={inbound.pendingIdentifiers}
            selectedId={inbound.selectedId}
            onCreate={inbound.createDemo}
            onOpenCreate={inbound.openCreate}
            onReceiveNextBatch={inbound.receiveNextBatch}
            onRefresh={inbound.refreshSelected}
            onSelect={inbound.selectInbound}
          />
        )}
        {activeView === 'inventory' && (
          <InventoryPage
            busy={scenario.busy}
            error={scenario.error}
            selected={scenario.selectedShipment}
            onBack={() => setActiveView('shipment')}
            onRefresh={scenario.refreshSelected}
          />
        )}
        {activeView === 'transfer' && <TransferPage />}
      </AppShell>
    </ConfigProvider>
  );
}
