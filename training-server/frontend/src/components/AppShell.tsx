import { useEffect, useState, type ReactNode } from 'react';
import {
  AppstoreOutlined,
  BellOutlined,
  InboxOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  SendOutlined,
  SettingOutlined,
  SwapOutlined,
} from '@ant-design/icons';
import { Avatar, Badge, Breadcrumb, Button, Layout, Menu, Space, Tabs, Tag, Tooltip } from 'antd';
import type { MenuProps } from 'antd';
import type { ViewKey } from '../types';

const { Header, Sider, Content } = Layout;

const menuItems: MenuProps['items'] = [
  { key: 'overview', icon: <AppstoreOutlined />, label: '工作台', disabled: true },
  {
    key: 'outbound',
    icon: <SendOutlined />,
    label: '出库管理',
    children: [
      { key: 'shipment', label: '出库订单' },
      { key: 'picking', label: '拣货作业', disabled: true },
    ],
  },
  {
    key: 'warehouse',
    icon: <InboxOutlined />,
    label: '仓储管理',
    children: [
      { key: 'inventory', label: '库存明细' },
      { key: 'transfer', label: '移库作业', disabled: true },
    ],
  },
  { key: 'integration', icon: <SwapOutlined />, label: '平台编排', disabled: true },
  { key: 'settings', icon: <SettingOutlined />, label: '系统设置', disabled: true },
];

const tabLabels: Record<ViewKey, string> = {
  shipment: '出库订单',
  inventory: '库存明细',
};

interface AppShellProps {
  activeView: ViewKey;
  children: ReactNode;
  onViewChange: (view: ViewKey) => void;
}

export function AppShell({ activeView, children, onViewChange }: AppShellProps) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobile, setMobile] = useState(window.innerWidth <= 768);

  useEffect(() => {
    const update = () => {
      const nextMobile = window.innerWidth <= 768;
      setMobile(nextMobile);
      if (nextMobile) setCollapsed(true);
    };
    window.addEventListener('resize', update);
    return () => window.removeEventListener('resize', update);
  }, []);

  return (
    <Layout className="app-layout">
      <Sider
        className="app-sider"
        collapsed={collapsed}
        collapsedWidth={mobile ? 0 : 64}
        trigger={null}
        width={224}
      >
        <button className="brand" type="button" onClick={() => onViewChange('shipment')}>
          <span className="brand-mark">S</span>
          {!collapsed && <span className="brand-copy">South WMS</span>}
        </button>
        <Menu
          mode="inline"
          theme="dark"
          selectedKeys={[activeView]}
          defaultOpenKeys={['outbound', 'warehouse']}
          items={menuItems}
          onClick={({ key }) => {
            if (key === 'shipment' || key === 'inventory') onViewChange(key);
            if (mobile) setCollapsed(true);
          }}
        />
        {!collapsed && (
          <div className="sider-footer">
            <span className="status-dot" />
            课堂服务已连接
          </div>
        )}
      </Sider>

      <Layout className="workspace">
        <Header className="top-header">
          <div className="header-left">
            <Button
              aria-label={collapsed ? '展开菜单' : '收起菜单'}
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              type="text"
              onClick={() => setCollapsed((value) => !value)}
            />
            <Breadcrumb items={[{ title: '首页' }, { title: tabLabels[activeView] }]} />
          </div>
          <Space size="middle">
            <Tooltip title="搜索（课堂版未启用）">
              <Button aria-label="搜索" icon={<SearchOutlined />} type="text" />
            </Tooltip>
            <Badge dot>
              <Button aria-label="通知" icon={<BellOutlined />} type="text" />
            </Badge>
            <Tag color="blue">课堂环境</Tag>
            <Avatar className="operator-avatar" shape="square">讲</Avatar>
            <span className="operator-name">课程讲师</span>
          </Space>
        </Header>

        <div className="view-tabs">
          <Tabs
            activeKey={activeView}
            items={Object.entries(tabLabels).map(([key, label]) => ({ key, label }))}
            onChange={(key) => onViewChange(key as ViewKey)}
          />
        </div>

        <Content className="content-area">{children}</Content>
      </Layout>
    </Layout>
  );
}
