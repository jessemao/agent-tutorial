import { useMemo, useState } from 'react';
import {
  CloseCircleOutlined,
  PlusOutlined,
  ReloadOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { SCENARIO } from '../api/wms';
import type { ScenarioIdentifiers, TrainingShipment } from '../types';

const { Title, Text } = Typography;

const statusLabels = {
  CREATED: '待预占',
  RESERVED: '已预占',
  SHIPPED: '已出库',
  CANCELLED: '已取消',
};

interface ShipmentPageProps {
  busy: boolean;
  error?: string;
  pendingIdentifiers?: ScenarioIdentifiers;
  selectedId?: number;
  shipments: TrainingShipment[];
  onCancel: (reason: string) => Promise<boolean>;
  onCreate: () => Promise<boolean>;
  onInventory: () => void;
  onOpenCreate: () => ScenarioIdentifiers;
  onRefresh: () => Promise<boolean>;
  onSelect: (id: number) => void;
}

export function ShipmentPage(props: ShipmentPageProps) {
  const [createOpen, setCreateOpen] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [orderKeyword, setOrderKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>();
  const [cancelReason, setCancelReason] = useState<string>(SCENARIO.defaultCancelReason);

  const selected = props.shipments.find((shipment) => shipment.shipmentId === props.selectedId);
  const filtered = useMemo(
    () =>
      props.shipments.filter(
        (shipment) =>
          (!orderKeyword || shipment.orderNo.toLowerCase().includes(orderKeyword.toLowerCase())) &&
          (!statusFilter || shipment.status === statusFilter),
      ),
    [orderKeyword, props.shipments, statusFilter],
  );

  const columns: ColumnsType<TrainingShipment> = [
    { title: '出库单号', dataIndex: 'orderNo', width: 210, fixed: 'left', render: (value) => <a>{value}</a> },
    { title: '出库类型', width: 120, render: () => '常规出库单' },
    { title: '货主名称', width: 120, render: () => '课堂货主' },
    { title: '仓库名称', width: 120, render: () => '天津仓库' },
    { title: '客户名称', width: 120, render: () => '嘉盛商贸' },
    {
      title: '订单状态',
      dataIndex: 'status',
      width: 110,
      render: (status: TrainingShipment['status']) => (
        <Tag color={status === 'CANCELLED' ? 'default' : status === 'RESERVED' ? 'processing' : 'green'}>
          {statusLabels[status]}
        </Tag>
      ),
    },
    { title: '商品数量', dataIndex: 'quantity', width: 100, align: 'right' },
    {
      title: '有效库存',
      width: 100,
      align: 'right',
      render: (_, shipment) => shipment.inventory.availableQuantity,
    },
    {
      title: '占位库存',
      width: 100,
      align: 'right',
      render: (_, shipment) => (
        <Text type={shipment.status === 'CANCELLED' && shipment.inventory.reservedQuantity > 0 ? 'danger' : undefined}>
          {shipment.inventory.reservedQuantity}
        </Text>
      ),
    },
    { title: '取消原因', dataIndex: 'cancelReason', width: 140, render: (value) => value || '-' },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      width: 180,
      render: (value: Date) => value.toLocaleString('zh-CN', { hour12: false }),
    },
  ];

  function showCreate(): void {
    props.onOpenCreate();
    setCreateOpen(true);
  }

  async function confirmCreate(): Promise<void> {
    if (await props.onCreate()) setCreateOpen(false);
  }

  async function confirmCancel(): Promise<void> {
    if (await props.onCancel(cancelReason)) setCancelOpen(false);
  }

  return (
    <section className="page-section" aria-labelledby="shipment-page-title">
      <div className="page-heading">
        <div>
          <Text className="page-kicker">OUTBOUND OPERATIONS</Text>
          <Title id="shipment-page-title" level={2}>出库订单</Title>
        </div>
        <Text type="secondary">选择已预占单据执行取消，再到库存明细核对最终数据。</Text>
      </div>

      {props.error && <Alert className="page-alert" message="操作未完成" description={props.error} type="error" showIcon />}

      <Card className="filter-card" bordered={false}>
        <Form layout="inline">
          <Form.Item label="出库单号">
            <Input allowClear placeholder="请输入出库单号" value={orderKeyword} onChange={(event) => setOrderKeyword(event.target.value)} />
          </Form.Item>
          <Form.Item label="订单状态">
            <Select
              allowClear
              placeholder="全部状态"
              value={statusFilter}
              style={{ width: 150 }}
              options={Object.entries(statusLabels).map(([value, label]) => ({ value, label }))}
              onChange={setStatusFilter}
            />
          </Form.Item>
          <Form.Item label="仓库名称"><Input disabled value="天津仓库" /></Form.Item>
          <Form.Item>
            <Button type="primary">查询</Button>
          </Form.Item>
          <Form.Item>
            <Button onClick={() => { setOrderKeyword(''); setStatusFilter(undefined); }}>重置</Button>
          </Form.Item>
        </Form>
      </Card>

      <Card className="table-card" bordered={false}>
        <div className="table-toolbar">
          <Space wrap>
            <Button id="open-create-dialog" icon={<PlusOutlined />} type="primary" onClick={showCreate}>新增演示单</Button>
            <Button
              id="open-cancel-dialog"
              danger
              disabled={selected?.status !== 'RESERVED'}
              icon={<CloseCircleOutlined />}
              onClick={() => setCancelOpen(true)}
            >
              取消出库
            </Button>
            <Button icon={<ReloadOutlined />} loading={props.busy} onClick={props.onRefresh}>刷新</Button>
            <Button icon={<UnorderedListOutlined />} onClick={props.onInventory}>库存明细</Button>
          </Space>
          <Text type="secondary">{selected ? `已选择：${selected.orderNo}` : '请选择一条出库单'}</Text>
        </div>
        <Table
          id="shipment-table"
          columns={columns}
          dataSource={filtered}
          loading={props.busy}
          rowKey="shipmentId"
          rowSelection={{
            type: 'radio',
            selectedRowKeys: props.selectedId ? [props.selectedId] : [],
            onChange: ([key]) => props.onSelect(Number(key)),
          }}
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 1450 }}
          locale={{ emptyText: '暂无出库订单，请先新增演示单' }}
          onRow={(shipment) => ({ onClick: () => props.onSelect(shipment.shipmentId) })}
        />
      </Card>

      <Modal
        title="新建出库订单"
        open={createOpen}
        okText="确定"
        cancelText="取消"
        confirmLoading={props.busy}
        onCancel={() => setCreateOpen(false)}
        onOk={confirmCreate}
      >
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="出库单号" span={2}>{props.pendingIdentifiers?.orderNo}</Descriptions.Item>
          <Descriptions.Item label="商品编号" span={2}>{props.pendingIdentifiers?.skuId}</Descriptions.Item>
          <Descriptions.Item label="仓库">天津仓库</Descriptions.Item>
          <Descriptions.Item label="出库数量">{SCENARIO.shipmentQuantity}</Descriptions.Item>
          <Descriptions.Item label="货主">课堂货主</Descriptions.Item>
          <Descriptions.Item label="客户">嘉盛商贸</Descriptions.Item>
        </Descriptions>
        <Alert className="modal-note" type="info" showIcon message="系统将准备 10 件库存，并为本单预占 6 件。" />
      </Modal>

      <Modal
        title="取消出库"
        open={cancelOpen}
        okText="确认取消"
        okButtonProps={{ danger: true }}
        cancelText="返回"
        confirmLoading={props.busy}
        onCancel={() => setCancelOpen(false)}
        onOk={confirmCancel}
      >
        <p>确定取消出库单 <strong>{selected?.orderNo}</strong> 吗？</p>
        <Form layout="vertical">
          <Form.Item label="取消原因" required>
            <Input.TextArea maxLength={100} rows={3} value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} />
          </Form.Item>
        </Form>
      </Modal>
    </section>
  );
}
