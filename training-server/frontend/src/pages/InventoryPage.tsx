import { useMemo, useState } from 'react';
import { ArrowLeftOutlined, ReloadOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Form, Input, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { SCENARIO } from '../api/wms';
import type { TrainingShipment } from '../types';

const { Title, Text } = Typography;

interface InventoryPageProps {
  busy: boolean;
  error?: string;
  selected?: TrainingShipment;
  onBack: () => void;
  onRefresh: () => Promise<boolean>;
}

export function InventoryPage({ busy, error, selected, onBack, onRefresh }: InventoryPageProps) {
  const [skuKeyword, setSkuKeyword] = useState('');
  const visibleRows = useMemo(
    () => (selected && (!skuKeyword || String(selected.skuId).includes(skuKeyword)) ? [selected] : []),
    [selected, skuKeyword],
  );

  const columns: ColumnsType<TrainingShipment> = [
    { title: '库存 ID', width: 140, render: (_, row) => `INV-${String(row.skuId).slice(-6)}` },
    { title: '库存量', width: 100, align: 'right', render: (_, row) => row.inventory.availableQuantity + row.inventory.reservedQuantity },
    { title: '原始量', width: 100, align: 'right', render: () => SCENARIO.initialQuantity },
    {
      title: '占位量',
      width: 100,
      align: 'right',
      render: (_, row) => <Text type={row.status === 'CANCELLED' && row.inventory.reservedQuantity > 0 ? 'danger' : undefined}>{row.inventory.reservedQuantity}</Text>,
    },
    { title: '有效库存', width: 110, align: 'right', render: (_, row) => row.inventory.availableQuantity },
    { title: '货主名称', width: 120, render: () => '课堂货主' },
    { title: '执行单号', dataIndex: 'orderNo', width: 210 },
    { title: '来源类别', width: 140, render: () => '出库订单预占' },
    { title: '商品编号', dataIndex: 'skuId', width: 180 },
    { title: '仓库名称', width: 120, render: () => '天津仓库' },
    { title: '库区', width: 100, render: () => 'A 库区' },
    { title: '货位', width: 110, render: () => 'A-01-01' },
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

      {error && <Alert className="page-alert" message="查询未完成" description={error} type="error" showIcon />}
      <Alert className="inventory-audit" showIcon type={audit.type} message={audit.title} description={audit.description} />

      <Card className="filter-card" bordered={false}>
        <Form layout="inline">
          <Form.Item label="商品编号">
            <Input allowClear placeholder="请输入商品编号" value={skuKeyword} onChange={(event) => setSkuKeyword(event.target.value)} />
          </Form.Item>
          <Form.Item label="仓库名称"><Input disabled value="天津仓库" /></Form.Item>
          <Form.Item label="货位"><Input disabled value="A-01-01" /></Form.Item>
          <Form.Item><Button type="primary">查询</Button></Form.Item>
        </Form>
      </Card>

      <Card className="table-card" bordered={false}>
        <div className="table-toolbar">
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={onBack}>返回出库订单</Button>
            <Button icon={<ReloadOutlined />} loading={busy} onClick={onRefresh}>刷新</Button>
          </Space>
          <Text type="secondary">{selected ? `关联单据：${selected.orderNo}` : '尚未选择出库单'}</Text>
        </div>
        <Table
          id="inventory-table"
          columns={columns}
          dataSource={visibleRows}
          loading={busy}
          rowKey="shipmentId"
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 1430 }}
          locale={{ emptyText: '暂无库存明细，请先新增并选择出库单' }}
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
