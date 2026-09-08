import { useMemo, useState } from 'react';
import { InboxOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Descriptions, Form, Input, Modal, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { T02_SCENARIO } from '../api/wms';
import type { ScenarioIdentifiers, TrainingInbound } from '../types';

const { Title, Text } = Typography;

interface InboundPageProps {
  busy: boolean;
  error?: string;
  inbounds: TrainingInbound[];
  pendingIdentifiers?: ScenarioIdentifiers;
  selectedId?: number;
  onCreate: () => Promise<boolean>;
  onOpenCreate: () => ScenarioIdentifiers;
  onReceiveNextBatch: () => Promise<boolean>;
  onRefresh: () => Promise<boolean>;
  onSelect: (id: number) => void;
}

export function InboundPage(props: InboundPageProps) {
  const [createOpen, setCreateOpen] = useState(false);
  const [receiveOpen, setReceiveOpen] = useState(false);
  const [orderKeyword, setOrderKeyword] = useState('');
  const selected = props.inbounds.find((inbound) => inbound.inboundId === props.selectedId);
  const filtered = useMemo(
    () => props.inbounds.filter((inbound) => !orderKeyword || inbound.orderNo.includes(orderKeyword)),
    [orderKeyword, props.inbounds],
  );

  const columns: ColumnsType<TrainingInbound> = [
    { title: '入库单号', dataIndex: 'orderNo', width: 220, fixed: 'left', render: (value) => <a>{value}</a> },
    { title: '入库类型', width: 120, render: () => '采购入库' },
    { title: '供应商', width: 130, render: () => '华东供应商' },
    { title: '仓库', width: 120, render: () => '天津仓库' },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (status: TrainingInbound['status']) => (
        <Tag color={status === 'RECEIVED' ? 'green' : 'processing'}>
          {status === 'RECEIVED' ? '已完成' : status === 'PARTIALLY_RECEIVED' ? '部分收货' : '待收货'}
        </Tag>
      ),
    },
    { title: '计划数量', dataIndex: 'plannedQuantity', width: 110, align: 'right' },
    { title: '累计实收', dataIndex: 'receivedQuantity', width: 110, align: 'right' },
    { title: '待收数量', width: 110, align: 'right', render: (_, row) => row.plannedQuantity - row.receivedQuantity },
    { title: '有效库存', width: 110, align: 'right', render: (_, row) => row.inventory.availableQuantity },
    { title: '商品编号', dataIndex: 'skuId', width: 180 },
    { title: '创建时间', dataIndex: 'createdAt', width: 180, render: (value: Date) => value.toLocaleString('zh-CN', { hour12: false }) },
  ];

  function showCreate(): void {
    props.onOpenCreate();
    setCreateOpen(true);
  }

  async function confirmCreate(): Promise<void> {
    if (await props.onCreate()) setCreateOpen(false);
  }

  async function confirmReceipt(): Promise<void> {
    try {
      await props.onReceiveNextBatch();
    } finally {
      setReceiveOpen(false);
    }
  }

  return (
    <section className="page-section" aria-labelledby="inbound-page-title">
      <div className="page-heading">
        <div>
          <Text className="page-kicker">INBOUND RECEIVING</Text>
          <Title id="inbound-page-title" level={2}>入库订单</Title>
        </div>
        <Text type="secondary">验证旧版一次性收货规则能否支持供应商分批到货。</Text>
      </div>

      {props.error && (
        <Alert
          className="page-alert"
          message="入库操作未完成"
          description={`${props.error}；当前累计实收 ${selected?.receivedQuantity ?? 0}，待收 ${selected ? selected.plannedQuantity - selected.receivedQuantity : 0}，有效库存 ${selected?.inventory.availableQuantity ?? 0}。`}
          type="error"
          showIcon
        />
      )}

      <Card className="filter-card" bordered={false}>
        <Form layout="inline">
          <Form.Item label="入库单号">
            <Input allowClear placeholder="请输入入库单号" value={orderKeyword} onChange={(event) => setOrderKeyword(event.target.value)} />
          </Form.Item>
          <Form.Item label="仓库名称"><Input disabled value="天津仓库" /></Form.Item>
          <Form.Item><Button type="primary">查询</Button></Form.Item>
        </Form>
      </Card>

      <Card className="table-card" bordered={false}>
        <div className="table-toolbar">
          <Space wrap>
            <Button id="open-inbound-create-dialog" icon={<PlusOutlined />} type="primary" onClick={showCreate}>新增入库单</Button>
            <Button
              id="open-partial-receipt-dialog"
              icon={<InboxOutlined />}
              disabled={!selected || selected.status === 'RECEIVED'}
              onClick={() => setReceiveOpen(true)}
            >
              {selected?.status === 'PARTIALLY_RECEIVED' ? '登记剩余到货' : '登记首批到货'}
            </Button>
            <Button icon={<ReloadOutlined />} loading={props.busy} onClick={props.onRefresh}>刷新</Button>
          </Space>
          <Text type="secondary">{selected ? `已选择：${selected.orderNo}` : '请选择一条入库单'}</Text>
        </div>
        <Table
          id="inbound-table"
          columns={columns}
          dataSource={filtered}
          loading={props.busy}
          rowKey="inboundId"
          rowSelection={{
            type: 'radio',
            selectedRowKeys: props.selectedId ? [props.selectedId] : [],
            onChange: ([key]) => props.onSelect(Number(key)),
          }}
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 1480 }}
          locale={{ emptyText: '暂无入库订单，请先新增入库单' }}
          onRow={(inbound) => ({ onClick: () => props.onSelect(inbound.inboundId) })}
        />
      </Card>

      <Modal title="新建入库订单" open={createOpen} okText="确定" cancelText="取消" confirmLoading={props.busy} onCancel={() => setCreateOpen(false)} onOk={confirmCreate}>
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="入库单号" span={2}>{props.pendingIdentifiers?.orderNo}</Descriptions.Item>
          <Descriptions.Item label="商品编号" span={2}>{props.pendingIdentifiers?.skuId}</Descriptions.Item>
          <Descriptions.Item label="计划数量">{T02_SCENARIO.plannedQuantity}</Descriptions.Item>
          <Descriptions.Item label="仓库">天津仓库</Descriptions.Item>
        </Descriptions>
      </Modal>

      <Modal title={selected?.status === 'PARTIALLY_RECEIVED' ? '登记剩余到货' : '登记首批到货'} open={receiveOpen} okText="确认收货" cancelText="返回" confirmLoading={props.busy} onCancel={() => setReceiveOpen(false)} onOk={confirmReceipt}>
        <Descriptions bordered column={1} size="small">
          <Descriptions.Item label="入库单号">{selected?.orderNo}</Descriptions.Item>
          <Descriptions.Item label="计划数量">{T02_SCENARIO.plannedQuantity}</Descriptions.Item>
          <Descriptions.Item label="本次实收">{selected?.status === 'PARTIALLY_RECEIVED' ? T02_SCENARIO.finalReceiptQuantity : T02_SCENARIO.firstReceiptQuantity}</Descriptions.Item>
        </Descriptions>
        <Alert className="modal-note" type="info" showIcon message={selected?.status === 'PARTIALLY_RECEIVED' ? '供应商剩余 6 件已到达。' : '供应商首车到货 4 件，剩余 6 件稍后到达。'} />
      </Modal>
    </section>
  );
}
