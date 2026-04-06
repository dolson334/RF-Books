import { test, expect, APIRequestContext } from '@playwright/test';

const BASE = 'http://localhost:8081';
const ALIAS = 'testresort';

function url(path: string): string {
  const sep = path.includes('?') ? '&' : '?';
  return `${BASE}${path}${sep}resortAlias=${ALIAS}`;
}

async function createIncome(
  api: APIRequestContext,
  data: { incomeDate: string; amount: number; source: string; category?: string }
) {
  const res = await api.post(url('/api/income'), { data });
  expect(res.ok(), `create income failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

async function createExpense(
  api: APIRequestContext,
  data: { expenseDate: string; amount: number; vendorName: string; category?: string }
) {
  const res = await api.post(url('/api/expenses'), { data });
  expect(res.ok(), `create expense failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

async function cleanup(api: APIRequestContext) {
  await api.post(url('/api/test/cleanup'));
}

test.describe('Reports API', () => {
  test.beforeEach(async ({ request }) => {
    await cleanup(request);
  });

  test.afterAll(async ({ request }) => {
    await cleanup(request);
  });

  // ── Financial Summary ──

  test('summary returns totals for current month', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-${String(now.getDate()).padStart(2, '0')}`;

    await createIncome(request, {
      incomeDate: today,
      amount: 1000,
      source: 'Res #1',
      category: 'room_revenue',
    });
    await createIncome(request, {
      incomeDate: today,
      amount: 500,
      source: 'Res #2',
      category: 'food_beverage',
    });
    await createExpense(request, {
      expenseDate: today,
      amount: 300,
      vendorName: 'Sysco',
      category: 'food_beverage',
    });

    const res = await request.get(url('/api/reports/summary?period=MONTH'));
    expect(res.ok()).toBeTruthy();
    const summary = await res.json();

    expect(summary.totalIncome).toBe(1500);
    expect(summary.totalExpenses).toBe(300);
    expect(summary.netIncome).toBe(1200);
    expect(summary.incomeCount).toBe(2);
    expect(summary.expenseCount).toBe(1);
    expect(summary.period).toBeTruthy();
    expect(summary.startDate).toBeTruthy();
    expect(summary.endDate).toBeTruthy();
  });

  // ── Income by Category ──

  test('income-by-category returns grouped totals with percentages', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-15`;
    const startDate = `${y}-${m}-01`;
    const lastDay = new Date(y, now.getMonth() + 1, 0).getDate();
    const endDate = `${y}-${m}-${lastDay}`;

    await createIncome(request, { incomeDate: today, amount: 2000, source: 'A', category: 'room_revenue' });
    await createIncome(request, { incomeDate: today, amount: 1000, source: 'B', category: 'room_revenue' });
    await createIncome(request, { incomeDate: today, amount: 500, source: 'C', category: 'food_beverage' });

    const res = await request.get(
      url(`/api/reports/income-by-category?startDate=${startDate}&endDate=${endDate}`)
    );
    expect(res.ok()).toBeTruthy();
    const categories = await res.json();

    expect(categories.length).toBe(2);

    const room = categories.find((c: any) => c.category === 'room_revenue');
    expect(room).toBeTruthy();
    expect(room.total).toBe(3000);
    expect(room.count).toBe(2);
    expect(room.percentage).toBeCloseTo(85.7, 0);

    const food = categories.find((c: any) => c.category === 'food_beverage');
    expect(food).toBeTruthy();
    expect(food.total).toBe(500);
    expect(food.count).toBe(1);
  });

  // ── Expenses by Category ──

  test('expenses-by-category returns grouped totals', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-10`;
    const startDate = `${y}-${m}-01`;
    const lastDay = new Date(y, now.getMonth() + 1, 0).getDate();
    const endDate = `${y}-${m}-${lastDay}`;

    await createExpense(request, { expenseDate: today, amount: 500, vendorName: 'V1', category: 'payroll' });
    await createExpense(request, { expenseDate: today, amount: 200, vendorName: 'V2', category: 'utilities' });
    await createExpense(request, { expenseDate: today, amount: 300, vendorName: 'V3', category: 'payroll' });

    const res = await request.get(
      url(`/api/reports/expenses-by-category?startDate=${startDate}&endDate=${endDate}`)
    );
    expect(res.ok()).toBeTruthy();
    const categories = await res.json();

    expect(categories.length).toBe(2);

    const payroll = categories.find((c: any) => c.category === 'payroll');
    expect(payroll).toBeTruthy();
    expect(payroll.total).toBe(800);
    expect(payroll.count).toBe(2);

    const utilities = categories.find((c: any) => c.category === 'utilities');
    expect(utilities.total).toBe(200);
  });

  // ── Monthly Trend ──

  test('monthly-trend returns income and expense per month', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-05`;

    await createIncome(request, { incomeDate: today, amount: 4000, source: 'Big Event', category: 'activities' });
    await createExpense(request, { expenseDate: today, amount: 1500, vendorName: 'Vendor', category: 'supplies' });

    const res = await request.get(url('/api/reports/monthly-trend?months=3'));
    expect(res.ok()).toBeTruthy();
    const trend = await res.json();

    expect(trend.length).toBe(3);

    // Current month should have our data
    const currentMonth = trend[trend.length - 1];
    expect(currentMonth.month).toBe(`${y}-${m}`);
    expect(currentMonth.income).toBe(4000);
    expect(currentMonth.expenses).toBe(1500);
    expect(currentMonth.net).toBe(2500);

    // Previous months should be zero (we cleaned up)
    const prevMonth = trend[0];
    expect(prevMonth.income).toBe(0);
    expect(prevMonth.expenses).toBe(0);
  });

  // ── Summary with period = YEAR ──

  test('summary with YEAR period aggregates full year', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-01`;

    await createIncome(request, { incomeDate: today, amount: 9999, source: 'Annual', category: 'other_revenue' });

    const res = await request.get(url(`/api/reports/summary?period=YEAR&date=${today}`));
    expect(res.ok()).toBeTruthy();
    const summary = await res.json();

    expect(summary.totalIncome).toBe(9999);
    expect(summary.period).toBe(String(y));
    expect(summary.startDate).toBe(`${y}-01-01`);
    expect(summary.endDate).toBe(`${y}-12-31`);
  });

  // ── Reconciliation rate ──

  test('summary reflects reconciliation rate', async ({ request }) => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const today = `${y}-${m}-15`;

    // Create 2 income, mark 1 as reconciled via manual match
    const inc1 = await createIncome(request, { incomeDate: today, amount: 100, source: 'R1' });
    await createIncome(request, { incomeDate: today, amount: 200, source: 'R2' });

    // Seed a bank transaction and match it to inc1
    await request.post(url('/api/test/seed-bank-transaction'), {
      data: { transactionId: 'txn_rpt_1', amount: -100, date: today, name: 'Deposit' },
    });
    await request.post(url('/api/reconciliation/match/income'), {
      data: { incomeId: inc1.id, transactionId: 'txn_rpt_1' },
    });

    const res = await request.get(url('/api/reports/summary?period=MONTH'));
    expect(res.ok()).toBeTruthy();
    const summary = await res.json();

    expect(summary.reconciledCount).toBe(1);
    expect(summary.unreconciledCount).toBe(1);
    expect(summary.reconciliationRate).toBe(50.0);
  });
});
