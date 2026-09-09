import { useEffect, useMemo, useState } from 'react';
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Select, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { queryInventoryBalance, SCENARIO } from '../api/wms';
import type { InventoryBalance, TrainingShipment } from '../types';

const { Title, Text } = Typography;

interface InventoryPageProps {
  busy: boolean;
  error?: string;
  selected?: TrainingShipment;
  onBack: () => void;
  onRefresh: () => Promise<boolean>;
}

interface InventoryRow {
  key: string;
  skuId: number;
  inventory: InventoryBalance;
  originalQuantity: number;
  orderNo?: string;
  sourceCategory: string;
  locationCode: string;
}

const locations = [
  { label: 'A01-01-01', value: 1 },
  { label: 'A01-01-02', value: 2 },
];

export function InventoryPage({ busy, error, selected, onBack, onRefresh }: InventoryPageProps) {
  const [skuKeyword, setSkuKeyword] = useState(selected ? String(selected.skuId) : '303');
  const [locationId, setLocationId] = useState<number>();
  const [queriedRows, setQueriedRows] = useState<InventoryRow[]>([]);
  const [queryError, setQueryError] = useState<string>();
  const [querying, setQuerying] = useState(false);
  const selectedRow = useMemo<InventoryRow | undefined>(() => selected ? {
    key: `shipment-${selected.shipmentId}`,
    skuId: selected.skuId,
    inventory: selected.inventory,
    originalQuantity: SCENARIO.initialQuantity,
    orderNo: selected.orderNo,
    sourceCategory: '出库订单预占',
    locationCode: 'A01-01-01',
  } : undefined, [selected]);
  const visibleRows = useMemo(
    () => queriedRows.length ? queriedRows : selectedRow ? [selectedRow] : [],
    [queriedRows, selectedRow],
  );

  async function loadInventory() {
    const skuId = Number(skuKeyword);
    if (!Number.isSafeInteger(skuId) || skuId <= 0) {
      setQueryError('请输入有效的商品编号。');
      return;
    }
    setQuerying(true);
    setQueryError(undefined);
    try {
      const requestedLocations = locationId ? locations.filter((location) => location.value === locationId) : locations;
      const balances = await Promise.allSettled(
        requestedLocations.map(async (location) => ({ location, inventory: await queryInventoryBalance(skuId, location.value) })),
      );
      const rows = balances.flatMap((balance) => balance.status === 'fulfilled' ? [{
        key: `inventory-${skuId}-${balance.value.location.value}`,
        skuId,
        inventory: balance.value.inventory,
        originalQuantity: balance.value.inventory.availableQuantity + balance.value.inventory.reservedQuantity,
        sourceCategory: skuId === 303 ? 'T03 期初库存' : '库存余额',
        locationCode: balance.value.location.label,
      }] : []);
      setQueriedRows(rows);
      if (!rows.length) setQueryError('未查询到库存。');
    } catch (caught) {
      setQueryError(caught instanceof Error ? caught.message : '查询失败');
    } finally {
      setQuerying(false);
    }
  }

  useEffect(() => {
    if (!selected) void loadInventory();
  }, [selected]);

  const columns: ColumnsType<InventoryRow> = [
    { title: '库存 ID', width: 140, render: (_, row) => `INV-${String(row.skuId).slice(-6)}` },
    { title: '库存量', width: 100, align: 'right', render: (_, row) => row.inventory.availableQuantity + row.inventory.reservedQuantity },
    { title: '原始量', dataIndex: 'originalQuantity', width: 100, align: 'right' },
    {
      title: '占位量',
      width: 100,
      align: 'right',
      render: (_, row) => <Text>{row.inventory.reservedQuantity}</Text>,
    },
    { title: '有效库存', width: 110, align: 'right', render: (_, row) => row.inventory.availableQuantity },
    { title: '货主名称', width: 120, render: () => '课堂货主' },
    { title: '执行单号', dataIndex: 'orderNo', width: 210, render: (value) => value ?? '—' },
    { title: '来源类别', dataIndex: 'sourceCategory', width: 140 },
    { title: '商品编号', dataIndex: 'skuId', width: 180 },
    { title: '仓库名称', width: 120, render: () => '天津仓库' },
    { title: '库区', width: 100, render: () => 'A 库区' },
    { title: '货位', dataIndex: 'locationCode', width: 110 },
  ];

  const audit = inventoryAudit(selected);

  return (
    <section className="page-section" id="inventory-view" aria-labelledby="inventory-page-title">
      <div className="page-heading">
        <div>
          <Text className="page-kicker">INVENTORY VISIBILITY</Text>
          <Title id="inventory-page-title" level={2}>库存明细</Title>
        </div>
        <Text type="secondary">核对单据取消后的有效库存和占位库存，不在页面预设代码根因。</Text>
      </div>

      {(error || queryError) && <Alert className="page-alert" message="查询未完成" description={queryError ?? error} type="error" showIcon />}
      {selected ? (
        <Alert className="inventory-audit" showIcon type={audit.type} message={audit.title} description={audit.description} />
      ) : (
        <Alert className="inventory-audit" showIcon type="info" message="独立库存查询" description="当前未关联出库单，可直接按商品编号查询库存。" />
      )}

      <Card className="filter-card" bordered={false}>
        <Form layout="inline">
          <Form.Item label="商品编号">
            <Input allowClear placeholder="请输入商品编号" value={skuKeyword} onChange={(event) => setSkuKeyword(event.target.value)} />
          </Form.Item>
          <Form.Item label="仓库名称"><Input disabled value="天津仓库" /></Form.Item>
          <Form.Item label="货位">
            <Select allowClear placeholder="全部货位" style={{ width: 180 }} options={locations} value={locationId} onChange={setLocationId} />
          </Form.Item>
          <Form.Item><Button type="primary" loading={querying} onClick={() => void loadInventory()}>查询</Button></Form.Item>
        </Form>
      </Card>

      <Card className="table-card" bordered={false}>
        <div className="table-toolbar">
          <Space>
            {selected && <Button icon={<ArrowLeftOutlined />} onClick={onBack}>返回出库订单</Button>}
            <Button icon={<ReloadOutlined />} loading={busy || querying} onClick={() => {
              if (selected) void onRefresh();
              else void loadInventory();
            }}>刷新</Button>
          </Space>
          <Text type="secondary">{selected ? `关联单据：${selected.orderNo}` : '独立库存视图'}</Text>
        </div>
        <Table
          id="inventory-table"
          columns={columns}
          dataSource={visibleRows}
          loading={busy}
          rowKey="key"
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 1430 }}
          locale={{ emptyText: '暂无库存明细，请输入商品编号查询' }}
        />
      </Card>
    </section>
  );
}

function inventoryAudit(selected?: TrainingShipment): {
  type: 'info' | 'success' | 'error';
  title: string;
  description: string;
} {
  if (!selected || selected.status !== 'CANCELLED') {
    return { type: 'info', title: '等待取消出库', description: '取消单据后刷新库存，核对占位量是否归零。' };
  }
  if (selected.inventory.availableQuantity === SCENARIO.initialQuantity && selected.inventory.reservedQuantity === 0) {
    return { type: 'success', title: '库存校验正常', description: '出库单已取消，占位库存为 0，有效库存已恢复为 10。' };
  }
  return {
    type: 'error',
    title: '库存校验异常',
    description: `出库单已取消，但占位库存仍为 ${selected.inventory.reservedQuantity}，有效库存仅为 ${selected.inventory.availableQuantity}。`,
  };
}
