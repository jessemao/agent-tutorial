import { expect, type Page, test } from '@playwright/test';

async function openInventoryCount(page: Page) {
  await page.goto('/', { waitUntil: 'domcontentloaded' });
  await page.getByText('库存盘点', { exact: true }).first().click();
  await expect(page.getByRole('heading', { name: '库存盘点' })).toBeVisible();
}

async function expectStatus(page: Page, status: string) {
  await expect(page.getByText(new RegExp(`状态：.*${status}`))).toBeVisible();
}

async function createAndSubmitCount(page: Page, countedQuantity: string) {
  const inputs = page.getByRole('spinbutton');
  await expect(inputs).toHaveCount(4);
  await inputs.nth(3).fill(countedQuantity);

  await page.getByRole('button', { name: '创建盘点' }).click();
  await expectStatus(page, 'DRAFT');
  await page.getByRole('button', { name: '确认实盘' }).click();
  await expectStatus(page, 'COUNTED');
  await page.getByRole('button', { name: '提交', exact: true }).click();
  await expectStatus(page, 'SUBMITTED');
}

test('用户可完成盘点并在刷新后看到调整结果', async ({ page }) => {
  await openInventoryCount(page);
  await createAndSubmitCount(page, '12');

  await page.getByRole('button', { name: '审批并调整' }).click();
  await expectStatus(page, 'ADJUSTED');
  await expect(page.getByText(/差异：2/)).toBeVisible();
  await expect(page.getByText(/流水：1 条/)).toBeVisible();

  await page.getByRole('button', { name: '刷新' }).click();
  await expectStatus(page, 'ADJUSTED');
  await expect(page.getByText(/差异：2/)).toBeVisible();
});

test('用户驳回盘点后状态可刷新且不产生调整流水', async ({ page }) => {
  await openInventoryCount(page);
  await createAndSubmitCount(page, '9');

  await page.getByRole('button', { name: '驳回' }).click();
  await expectStatus(page, 'REJECTED');
  await expect(page.getByText(/流水：/)).toHaveCount(0);

  await page.getByRole('button', { name: '刷新' }).click();
  await expectStatus(page, 'REJECTED');
  await expect(page.getByText(/流水：/)).toHaveCount(0);
});
