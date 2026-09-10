import { useEffect, useState } from 'react';
import { Alert, Button, Card, Descriptions, Form, Input, InputNumber, Space, Table, Typography } from 'antd';
import { queryTransferTasks, transferInventory } from '../api/wms';
import type { InventoryTransferTask, InventoryTransferView } from '../types';

const { Title, Text } = Typography;

const locations = [{ id: 1, code: 'A01-01-01' }, { id: 2, code: 'A01-01-02' }];
const locationId = (code: string) => locations.find(location => location.code === code)?.id ?? 0;
const locationCode = (id: number) => locations.find(location => location.id === id)?.code ?? String(id);

const newTransferNo = () => `TR-T03-${Date.now()}`;

export function TransferPage() {
  const [skuId, setSkuId] = useState(303);
  const [sourceLocation, setSourceLocation] = useState('A01-01-01');
  const [targetLocation, setTargetLocation] = useState('A01-01-02');
  const [quantity, setQuantity] = useState(4);
  const [transferNo, setTransferNo] = useState(newTransferNo);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState<InventoryTransferView>();
  const [tasks, setTasks] = useState<InventoryTransferTask[]>([]);
  const [completedRequest, setCompletedRequest] = useState({ skuId, sourceLocation, targetLocation, quantity });
  const [error, setError] = useState<string>();

  async function run(action: () => Promise<void>) {
    setBusy(true);
    setError(undefined);
    setResult(undefined);
    try {
      await action();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '操作失败');
    } finally {
      setBusy(false);
    }
  }

  async function loadTasks() {
    setTasks(await queryTransferTasks());
  }

  useEffect(() => { void loadTasks(); }, []);

  return (
    <section className="page-section" aria-labelledby="transfer-page-title">
      <div className="page-heading">
        <div>
          <Text className="page-kicker">INVENTORY TRANSFER</Text>
          <Title id="transfer-page-title" level={2}>移库作业</Title>
        </div>
        <Text type="secondary">将同一仓库内一个库位的可用库存原子移动到另一个库位。</Text>
      </div>

      {error && <Alert className="page-alert" message="移库操作未完成" description={error} type="error" showIcon />}
      {result && (
        <Alert
          className="page-alert"
          message={`移库单 ${result.transferNo} 已完成`}
          description={`源库位剩余 ${result.source.availableQuantity}，目标库位现有 ${result.target.availableQuantity}。`}
          type="success"
          showIcon
        />
      )}

      <Card className="filter-card" bordered={false}>
        <Form layout="vertical">
          <Space wrap align="end">
            <Form.Item label="商品编号"><InputNumber min={1} value={skuId} onChange={(value) => setSkuId(value ?? 303)} /></Form.Item>
            <Form.Item label="仓库"><Input disabled value="天津仓库" /></Form.Item>
            <Form.Item label="源库位"><Input value={sourceLocation} onChange={(event) => setSourceLocation(event.target.value)} /></Form.Item>
            <Form.Item label="目标库位"><Input value={targetLocation} onChange={(event) => setTargetLocation(event.target.value)} /></Form.Item>
            <Form.Item label="移库数量"><InputNumber min={1} value={quantity} onChange={(value) => setQuantity(value ?? 1)} /></Form.Item>
          </Space>
          <Space>
            <Button type="primary" loading={busy} onClick={() => run(async () => {
              const completed = await transferInventory({
                skuId,
                sourceLocationId: locationId(sourceLocation),
                targetLocationId: locationId(targetLocation),
                quantity,
                transferNo,
              });
              setResult(completed);
              setCompletedRequest({ skuId, sourceLocation, targetLocation, quantity });
              setTransferNo(newTransferNo());
              await loadTasks();
            })}>确认移库</Button>
          </Space>
        </Form>
      </Card>

      {result && (
        <Card className="table-card" bordered={false}>
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="移库单号">{result.transferNo}</Descriptions.Item>
            <Descriptions.Item label="商品编号">{completedRequest.skuId}</Descriptions.Item>
            <Descriptions.Item label="源库位">{completedRequest.sourceLocation}</Descriptions.Item>
            <Descriptions.Item label="目标库位">{completedRequest.targetLocation}</Descriptions.Item>
            <Descriptions.Item label="移库数量">{completedRequest.quantity}</Descriptions.Item>
            <Descriptions.Item label="源库位可用量">{result.source.availableQuantity}</Descriptions.Item>
            <Descriptions.Item label="目标库位可用量">{result.target.availableQuantity}</Descriptions.Item>
            <Descriptions.Item label="源库位占用量">{result.source.reservedQuantity}</Descriptions.Item>
            <Descriptions.Item label="目标库位占用量">{result.target.reservedQuantity}</Descriptions.Item>
          </Descriptions>
        </Card>
      )}


      <Card className="table-card" bordered={false} title="移库任务列表" extra={<Button onClick={() => void loadTasks()}>刷新</Button>}>
        <Table
          rowKey={(task) => `${task.transferNo}-${task.createdAt}`}
          dataSource={tasks}
          pagination={false}
          columns={[
            { title: '移库单号', dataIndex: 'transferNo' },
            { title: '商品编号', dataIndex: 'skuId' },
            { title: '源库位', render: (_, task) => locationCode(task.sourceLocationId) },
            { title: '目标库位', render: (_, task) => locationCode(task.targetLocationId) },
            { title: '数量', dataIndex: 'quantity' },
            { title: '状态', render: () => '已完成' },
            { title: '完成时间', render: (_, task) => new Date(task.createdAt).toLocaleString() },
          ]}
          locale={{ emptyText: '暂无移库任务' }}
        />
      </Card>
    </section>
  );
}
