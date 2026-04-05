import { test, expect, APIRequestContext } from '@playwright/test';

const BASE = 'http://localhost:8081';
const ALIAS = 'testresort';

/** helper — append resortAlias param */
function url(path: string): string {
  const sep = path.includes('?') ? '&' : '?';
  return `${BASE}${path}${sep}resortAlias=${ALIAS}`;
}

// ──────────────────────────────────────────────
//  Seed helpers
// ──────────────────────────────────────────────

async function createIncome(
  api: APIRequestContext,
  data: {
    incomeDate: string;
    amount: number;
    source: string;
    category?: string;
    paymentMethod?: string;
    referenceNumber?: string;
    description?: string;
  }
) {
  const res = await api.post(url('/api/income'), { data });
  expect(res.ok(), `create income failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

async function createExpense(
  api: APIRequestContext,
  data: {
    expenseDate: string;
    amount: number;
    vendorName: string;
    category?: string;
    paymentMethod?: string;
    referenceNumber?: string;
    description?: string;
  }
) {
  const res = await api.post(url('/api/expenses'), { data });
  expect(res.ok(), `create expense failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

async function seedBankTransaction(
  api: APIRequestContext,
  data: {
    transactionId: string;
    accountId?: string;
    amount: number;
    date: string;
    name: string;
    merchantName?: string;
    category?: string;
  }
) {
  // Directly insert a plaid_transactions row via a small helper endpoint
  // Since there is no direct endpoint, we use SQL through the test-seed endpoint
  // Fallback: use internal API if available, otherwise insert via the entity endpoint
  const res = await api.post(url('/api/test/seed-bank-transaction'), { data });
  expect(res.ok(), `seed bank txn failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

async function cleanup(api: APIRequestContext) {
  await api.post(url('/api/test/cleanup'));
}

// ──────────────────────────────────────────────
//  Tests
// ──────────────────────────────────────────────

test.describe('Reconciliation API', () => {
  test.beforeEach(async ({ request }) => {
    await cleanup(request);
  });

  test.afterAll(async ({ request }) => {
    await cleanup(request);
  });

  // ── Income CRUD ──

  test('create and list income', async ({ request }) => {
    const income = await createIncome(request, {
      incomeDate: '2026-03-15',
      amount: 1500.0,
      source: 'Reservation #100',
      category: 'Room Revenue',
      paymentMethod: 'Credit Card',
    });

    expect(income.id).toBeTruthy();
    expect(income.amount).toBe(1500.0);
    expect(income.source).toBe('Reservation #100');

    const list = await (await request.get(url('/api/income'))).json();
    expect(list.length).toBeGreaterThanOrEqual(1);
    expect(list.some((i: any) => i.id === income.id)).toBeTruthy();
  });

  // ── Expense CRUD ──

  test('create and list expenses', async ({ request }) => {
    const expense = await createExpense(request, {
      expenseDate: '2026-03-10',
      amount: 250.0,
      vendorName: 'Office Depot',
      category: 'Supplies',
      paymentMethod: 'Debit Card',
    });

    expect(expense.id).toBeTruthy();
    expect(expense.amount).toBe(250.0);

    const list = await (await request.get(url('/api/expenses'))).json();
    expect(list.length).toBeGreaterThanOrEqual(1);
    expect(list.some((e: any) => e.id === expense.id)).toBeTruthy();
  });

  // ── Import income (bulk) ──

  test('bulk import income with deduplication', async ({ request }) => {
    const payload = [
      {
        externalId: 'res_100_pay_1',
        incomeDate: '2026-03-15',
        amount: 500.0,
        source: 'Reservation #100',
        category: 'Room Revenue',
      },
      {
        externalId: 'res_101_pay_2',
        incomeDate: '2026-03-16',
        amount: 750.0,
        source: 'Reservation #101',
        category: 'Room Revenue',
      },
    ];

    // First import — both created
    const r1 = await request.post(url('/api/income/import'), { data: payload });
    expect(r1.ok()).toBeTruthy();
    const result1 = await r1.json();
    expect(result1.created).toBe(2);
    expect(result1.skipped).toBe(0);

    // Second import — both skipped (dedup by externalId)
    const r2 = await request.post(url('/api/income/import'), { data: payload });
    expect(r2.ok()).toBeTruthy();
    const result2 = await r2.json();
    expect(result2.created).toBe(0);
    expect(result2.skipped).toBe(2);
  });

  // ── Manual income match ──

  test('manual income match and unmatch', async ({ request }) => {
    // 1. Create income
    const income = await createIncome(request, {
      incomeDate: '2026-03-20',
      amount: 800.0,
      source: 'Reservation #200',
      category: 'Room Revenue',
    });

    // 2. Seed a matching bank transaction (credit = negative in Plaid)
    const txn = await seedBankTransaction(request, {
      transactionId: 'txn_credit_200',
      amount: -800.0,
      date: '2026-03-20',
      name: 'Reservation Payment #200',
    });

    // 3. Manually match
    const matchRes = await request.post(url('/api/reconciliation/match/income'), {
      data: { incomeId: income.id, transactionId: 'txn_credit_200' },
    });
    expect(matchRes.ok()).toBeTruthy();

    // 4. Verify income is now reconciled
    const incomeAfter = await (await request.get(url(`/api/income/${income.id}`))).json();
    expect(incomeAfter.reconciled).toBe(true);

    // 5. Verify match appears in list
    const matches = await (await request.get(url('/api/reconciliation/matches/income'))).json();
    expect(matches.some((m: any) => m.incomeId === income.id)).toBeTruthy();

    // 6. Unmatch
    const unmatchRes = await request.delete(url(`/api/reconciliation/match/income/${income.id}`));
    expect(unmatchRes.ok()).toBeTruthy();

    // 7. Verify income is unreconciled again
    const incomeAfter2 = await (await request.get(url(`/api/income/${income.id}`))).json();
    expect(incomeAfter2.reconciled).toBe(false);
  });

  // ── Manual expense match ──

  test('manual expense match and unmatch', async ({ request }) => {
    // 1. Create expense
    const expense = await createExpense(request, {
      expenseDate: '2026-03-10',
      amount: 350.0,
      vendorName: 'Home Depot',
      category: 'Maintenance',
    });

    // 2. Seed a matching bank transaction (debit = positive in Plaid)
    await seedBankTransaction(request, {
      transactionId: 'txn_debit_350',
      amount: 350.0,
      date: '2026-03-10',
      name: 'Home Depot',
      merchantName: 'Home Depot',
    });

    // 3. Manually match
    const matchRes = await request.post(url('/api/reconciliation/match/expense'), {
      data: { expenseId: expense.id, transactionId: 'txn_debit_350' },
    });
    expect(matchRes.ok()).toBeTruthy();

    // 4. Verify expense is reconciled
    const expAfter = await (await request.get(url(`/api/expenses/${expense.id}`))).json();
    expect(expAfter.reconciled).toBe(true);

    // 5. Unmatch
    await request.delete(url(`/api/reconciliation/match/expense/${expense.id}`));

    // 6. Verify unreconciled
    const expAfter2 = await (await request.get(url(`/api/expenses/${expense.id}`))).json();
    expect(expAfter2.reconciled).toBe(false);
  });

  // ── Auto-match / Smart Match ──

  test('auto-match generates suggestions and accepts them', async ({ request }) => {
    // 1. Create income with exact same amount + same date as a bank credit
    await createIncome(request, {
      incomeDate: '2026-03-25',
      amount: 1200.0,
      source: 'Reservation #300',
      category: 'Room Revenue',
    });

    // 2. Seed matching bank credit (amount exact, same date → score = 40 + 30 = 70 ≥ threshold)
    await seedBankTransaction(request, {
      transactionId: 'txn_auto_credit_300',
      amount: -1200.0,
      date: '2026-03-25',
      name: 'Reservation Payment',
    });

    // 3. Create expense with exact match to bank debit
    await createExpense(request, {
      expenseDate: '2026-03-26',
      amount: 99.99,
      vendorName: 'Amazon',
      category: 'Supplies',
    });

    await seedBankTransaction(request, {
      transactionId: 'txn_auto_debit_amazon',
      amount: 99.99,
      date: '2026-03-26',
      name: 'Amazon Marketplace',
      merchantName: 'Amazon',
    });

    // 4. Generate suggestions
    const sugRes = await request.post(url('/api/reconciliation/suggestions/generate'));
    expect(sugRes.ok()).toBeTruthy();
    const suggestions = await sugRes.json();
    expect(suggestions.length).toBeGreaterThanOrEqual(2);

    // Verify confidence scores meet threshold
    for (const s of suggestions) {
      expect(s.confidenceScore).toBeGreaterThanOrEqual(70);
      expect(s.status).toBe('PENDING');
    }

    // 5. Accept all suggestions
    const autoRes = await request.post(url('/api/reconciliation/auto-match'));
    expect(autoRes.ok()).toBeTruthy();
    const autoResult = await autoRes.json();
    expect(autoResult.accepted).toBeGreaterThanOrEqual(2);

    // 6. Verify all income/expenses are now reconciled
    const allIncome = await (await request.get(url('/api/income'))).json();
    const allExpenses = await (await request.get(url('/api/expenses'))).json();

    const reconciledIncome = allIncome.filter((i: any) => i.reconciled);
    const reconciledExpenses = allExpenses.filter((e: any) => e.reconciled);

    expect(reconciledIncome.length).toBeGreaterThanOrEqual(1);
    expect(reconciledExpenses.length).toBeGreaterThanOrEqual(1);
  });

  // ── Reconciliation summary ──

  test('reconciliation summary reflects correct counts', async ({ request }) => {
    // Seed 2 income + 1 expense + 2 bank transactions
    const inc1 = await createIncome(request, {
      incomeDate: '2026-03-01',
      amount: 500.0,
      source: 'Res #1',
    });
    await createIncome(request, {
      incomeDate: '2026-03-02',
      amount: 600.0,
      source: 'Res #2',
    });
    const exp1 = await createExpense(request, {
      expenseDate: '2026-03-05',
      amount: 100.0,
      vendorName: 'Vendor A',
    });

    await seedBankTransaction(request, {
      transactionId: 'txn_sum_credit_1',
      amount: -500.0,
      date: '2026-03-01',
      name: 'Deposit',
    });
    await seedBankTransaction(request, {
      transactionId: 'txn_sum_debit_1',
      amount: 100.0,
      date: '2026-03-05',
      name: 'Vendor A',
    });

    // Match 1 income + 1 expense
    await request.post(url('/api/reconciliation/match/income'), {
      data: { incomeId: inc1.id, transactionId: 'txn_sum_credit_1' },
    });
    await request.post(url('/api/reconciliation/match/expense'), {
      data: { expenseId: exp1.id, transactionId: 'txn_sum_debit_1' },
    });

    // Get summary
    const sumRes = await request.get(url('/api/reconciliation/summary'));
    expect(sumRes.ok()).toBeTruthy();
    const summary = await sumRes.json();

    expect(summary.matchedCount).toBe(2); // 1 income + 1 expense matched
    expect(summary.totalPayments).toBe(3); // 2 income + 1 expense
    expect(summary.unmatchedPaymentCount).toBe(1); // 1 unmatched income
    expect(summary.status).toBe('SUCCESS');
  });

  // ── Accept and reject individual suggestions ──

  test('accept individual suggestion then reject another', async ({ request }) => {
    // Seed a high-confidence match
    await createIncome(request, {
      incomeDate: '2026-04-01',
      amount: 2000.0,
      source: 'Res #400',
    });
    await seedBankTransaction(request, {
      transactionId: 'txn_ind_400',
      amount: -2000.0,
      date: '2026-04-01',
      name: 'Res 400 Payment',
    });

    // Seed a second match
    await createExpense(request, {
      expenseDate: '2026-04-02',
      amount: 45.0,
      vendorName: 'Staples',
    });
    await seedBankTransaction(request, {
      transactionId: 'txn_ind_staples',
      amount: 45.0,
      date: '2026-04-02',
      name: 'Staples Store',
      merchantName: 'Staples',
    });

    // Generate
    const sugRes = await request.post(url('/api/reconciliation/suggestions/generate'));
    const suggestions = await sugRes.json();
    expect(suggestions.length).toBeGreaterThanOrEqual(2);

    // Accept first suggestion
    const incomeSuggestion = suggestions.find((s: any) => s.incomeId != null);
    expect(incomeSuggestion).toBeTruthy();

    const acceptRes = await request.post(url(`/api/reconciliation/suggestions/${incomeSuggestion.id}/accept`));
    expect(acceptRes.ok()).toBeTruthy();

    // Reject second suggestion
    const expenseSuggestion = suggestions.find((s: any) => s.expenseId != null);
    expect(expenseSuggestion).toBeTruthy();

    const rejectRes = await request.post(url(`/api/reconciliation/suggestions/${expenseSuggestion.id}/reject`));
    expect(rejectRes.ok()).toBeTruthy();

    // Verify: income is reconciled, expense is NOT
    const allIncome = await (await request.get(url('/api/income'))).json();
    const allExpenses = await (await request.get(url('/api/expenses'))).json();

    const matchedIncome = allIncome.find((i: any) => i.id === incomeSuggestion.incomeId);
    expect(matchedIncome.reconciled).toBe(true);

    const unmatchedExpense = allExpenses.find((e: any) => e.id === expenseSuggestion.expenseId);
    expect(unmatchedExpense.reconciled).toBe(false);
  });
});
