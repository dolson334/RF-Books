import { test, expect, APIRequestContext } from '@playwright/test';

const BASE = 'http://localhost:8081';
const ALIAS = 'testresort';

function url(path: string): string {
  const sep = path.includes('?') ? '&' : '?';
  return `${BASE}${path}${sep}resortAlias=${ALIAS}`;
}

async function cleanup(api: APIRequestContext) {
  await api.post(url('/api/test/cleanup'));
}

test.describe('Expense Import API', () => {
  test.beforeEach(async ({ request }) => {
    await cleanup(request);
  });

  test.afterAll(async ({ request }) => {
    await cleanup(request);
  });

  test('import creates new expenses', async ({ request }) => {
    const payload = [
      {
        externalId: 'imp-001',
        expenseDate: '2026-04-01',
        amount: 150.0,
        vendorName: 'Office Depot',
        category: 'supplies',
        description: 'Office supplies',
      },
      {
        externalId: 'imp-002',
        expenseDate: '2026-04-02',
        amount: 300.0,
        vendorName: 'Sysco',
        category: 'food_beverage',
        description: 'Kitchen supplies',
      },
    ];

    const res = await request.post(url('/api/expenses/import'), { data: payload });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();

    expect(body.created).toBe(2);
    expect(body.skipped).toBe(0);
    expect(body.errors).toBe(0);
    expect(body.errorDetails).toHaveLength(0);

    // Verify expenses actually exist
    const listRes = await request.get(url('/api/expenses'));
    expect(listRes.ok()).toBeTruthy();
    const expenses = await listRes.json();
    expect(expenses.length).toBe(2);
    expect(expenses.some((e: any) => e.vendorName === 'Office Depot')).toBeTruthy();
    expect(expenses.some((e: any) => e.vendorName === 'Sysco')).toBeTruthy();
  });

  test('duplicate externalId is skipped', async ({ request }) => {
    const item = {
      externalId: 'dup-001',
      expenseDate: '2026-04-01',
      amount: 100.0,
      vendorName: 'Vendor A',
      category: 'utilities',
    };

    // First import
    const res1 = await request.post(url('/api/expenses/import'), { data: [item] });
    expect(res1.ok()).toBeTruthy();
    const body1 = await res1.json();
    expect(body1.created).toBe(1);
    expect(body1.skipped).toBe(0);

    // Second import — same externalId should be skipped
    const res2 = await request.post(url('/api/expenses/import'), { data: [item] });
    expect(res2.ok()).toBeTruthy();
    const body2 = await res2.json();
    expect(body2.created).toBe(0);
    expect(body2.skipped).toBe(1);

    // Should still be only 1 expense
    const listRes = await request.get(url('/api/expenses'));
    const expenses = await listRes.json();
    expect(expenses.length).toBe(1);
  });

  test('missing externalId returns error', async ({ request }) => {
    const payload = [
      {
        expenseDate: '2026-04-01',
        amount: 50.0,
        vendorName: 'No External ID',
        category: 'supplies',
      },
    ];

    const res = await request.post(url('/api/expenses/import'), { data: payload });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();

    expect(body.created).toBe(0);
    expect(body.errors).toBe(1);
    expect(body.errorDetails.length).toBe(1);
    expect(body.errorDetails[0]).toContain('externalId is required');
  });

  test('invalid category returns error', async ({ request }) => {
    const payload = [
      {
        externalId: 'cat-bad-001',
        expenseDate: '2026-04-01',
        amount: 75.0,
        vendorName: 'Bad Category Vendor',
        category: 'nonexistent_category',
      },
    ];

    const res = await request.post(url('/api/expenses/import'), { data: payload });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();

    expect(body.created).toBe(0);
    expect(body.errors).toBe(1);
    expect(body.errorDetails[0]).toContain('invalid category');
  });

  test('mixed batch: valid, duplicate, and invalid items', async ({ request }) => {
    // Pre-seed one expense to test duplicate detection
    const seed = [
      {
        externalId: 'mix-001',
        expenseDate: '2026-04-01',
        amount: 100.0,
        vendorName: 'Seed Vendor',
        category: 'utilities',
      },
    ];
    const seedRes = await request.post(url('/api/expenses/import'), { data: seed });
    expect(seedRes.ok()).toBeTruthy();

    // Now import a mixed batch
    const payload = [
      // Valid — new item
      {
        externalId: 'mix-002',
        expenseDate: '2026-04-02',
        amount: 200.0,
        vendorName: 'New Vendor',
        category: 'maintenance',
      },
      // Duplicate — same externalId as seed
      {
        externalId: 'mix-001',
        expenseDate: '2026-04-01',
        amount: 100.0,
        vendorName: 'Seed Vendor',
        category: 'utilities',
      },
      // Error — missing externalId
      {
        expenseDate: '2026-04-03',
        amount: 50.0,
        vendorName: 'No ID Vendor',
      },
      // Error — invalid category
      {
        externalId: 'mix-003',
        expenseDate: '2026-04-03',
        amount: 75.0,
        vendorName: 'Bad Cat Vendor',
        category: 'fake_category',
      },
      // Valid — another new item
      {
        externalId: 'mix-004',
        expenseDate: '2026-04-04',
        amount: 350.0,
        vendorName: 'Another Valid',
        category: 'payroll',
      },
    ];

    const res = await request.post(url('/api/expenses/import'), { data: payload });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();

    expect(body.created).toBe(2);   // mix-002 and mix-004
    expect(body.skipped).toBe(1);   // mix-001 duplicate
    expect(body.errors).toBe(2);    // missing externalId + invalid category
    expect(body.errorDetails).toHaveLength(2);

    // Total expenses should be 3: seed + 2 new
    const listRes = await request.get(url('/api/expenses'));
    const expenses = await listRes.json();
    expect(expenses.length).toBe(3);
  });
});
