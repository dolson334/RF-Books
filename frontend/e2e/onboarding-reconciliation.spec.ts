import { test, expect, APIRequestContext } from '@playwright/test';

const BASE = 'http://localhost:8081';
const ALIAS = 'testresort';

function url(path: string): string {
  const sep = path.includes('?') ? '&' : '?';
  return `${BASE}${path}${sep}resortAlias=${ALIAS}`;
}

// ──────────────────────────────────────────────
//  Helpers
// ──────────────────────────────────────────────

async function cleanup(api: APIRequestContext) {
  await api.post(url('/api/test/cleanup'));
}

/** Seed an income entry representing a reservation payment */
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

/** Seed a bank transaction representing what Plaid would pull from the bank */
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
  const res = await api.post(url('/api/test/seed-bank-transaction'), { data });
  expect(res.ok(), `seed bank txn failed: ${res.status()}`).toBeTruthy();
  return res.json();
}

// ──────────────────────────────────────────────
//  Demo reservation payment data
//  Simulates what the resort platform would generate:
//  guests check in, stay, pay — these show up as income
//  in RF-Books and as deposits in the bank account.
// ──────────────────────────────────────────────

const today = new Date().toISOString().slice(0, 10);
function daysAgo(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

const RESERVATION_PAYMENTS = [
  {
    guest: 'Anderson Family',
    category: 'room_revenue',
    amount: 1200.00,
    daysAgo: 1,
    ref: 'RES-1001',
    desc: 'Cabin 7 – 3 night stay',
    paymentMethod: 'card',
  },
  {
    guest: 'Martinez Wedding Party',
    category: 'food_beverage',
    amount: 4500.00,
    daysAgo: 2,
    ref: 'RES-1002',
    desc: 'Reception dinner & bar package',
    paymentMethod: 'ach',
  },
  {
    guest: 'Chen Family',
    category: 'room_revenue',
    amount: 2800.00,
    daysAgo: 3,
    ref: 'RES-1003',
    desc: 'Lake Suite – 5 night stay',
    paymentMethod: 'card',
  },
  {
    guest: 'Thompson Group',
    category: 'activities',
    amount: 750.00,
    daysAgo: 4,
    ref: 'RES-1004',
    desc: 'Guided fishing trip x6 guests',
    paymentMethod: 'card',
  },
  {
    guest: 'Davis Retreat',
    category: 'room_revenue',
    amount: 3600.00,
    daysAgo: 5,
    ref: 'RES-1005',
    desc: 'Executive Lodge – corporate retreat 4 nights',
    paymentMethod: 'ach',
  },
];

// Bank-side deposits that correspond to the reservation payments above.
// In Plaid, credits (money in) are represented as NEGATIVE amounts.
// Names reflect how they'd appear on a bank statement.
const BANK_DEPOSITS = [
  {
    txId: 'bank_dep_1001',
    amount: -1200.00,
    daysAgo: 1,
    name: 'POS Deposit - Anderson',
    merchant: 'Card Payment',
  },
  {
    txId: 'bank_dep_1002',
    amount: -4500.00,
    daysAgo: 2,
    name: 'ACH Credit - Martinez Wedding',
    merchant: 'ACH Deposit',
  },
  {
    txId: 'bank_dep_1003',
    amount: -2800.00,
    daysAgo: 3,
    name: 'POS Deposit - Chen',
    merchant: 'Card Payment',
  },
  {
    txId: 'bank_dep_1004',
    amount: -750.00,
    daysAgo: 4,
    name: 'POS Deposit - Thompson',
    merchant: 'Card Payment',
  },
  {
    txId: 'bank_dep_1005',
    amount: -3600.00,
    daysAgo: 5,
    name: 'ACH Credit - Davis Corp',
    merchant: 'ACH Deposit',
  },
  // One extra bank deposit with no matching reservation
  {
    txId: 'bank_dep_9999',
    amount: -999.00,
    daysAgo: 2,
    name: 'Unknown ACH Credit',
    merchant: 'Unknown',
  },
];

// ──────────────────────────────────────────────
//  Tests
// ──────────────────────────────────────────────

test.describe('Onboarding → Reconciliation E2E', () => {
  test.beforeEach(async ({ request }) => {
    await cleanup(request);
  });

  test.afterAll(async ({ request }) => {
    await cleanup(request);
  });

  test('full flow: seed reservations, onboard, connect bank, reconcile', async ({ request }) => {
    // ─── Step 1: Seed reservation payment data (income) ───────────
    // These represent payments collected by the resort's reservation system
    console.log('\n══════════════════════════════════════════════');
    console.log('  STEP 1: Seeding reservation payments (income)');
    console.log('══════════════════════════════════════════════');
    for (const pmt of RESERVATION_PAYMENTS) {
      await createIncome(request, {
        incomeDate: daysAgo(pmt.daysAgo),
        amount: pmt.amount,
        source: pmt.guest,
        category: pmt.category,
        paymentMethod: pmt.paymentMethod,
        referenceNumber: pmt.ref,
        description: pmt.desc,
      });
      console.log(`  ✓ ${pmt.ref} — ${pmt.guest}: $${pmt.amount.toFixed(2)} (${pmt.desc})`);
    }

    // Verify income was seeded
    const incomeRes = await request.get(url('/api/income'));
    expect(incomeRes.ok()).toBeTruthy();
    const incomeList = await incomeRes.json();
    expect(incomeList.length).toBe(5);
    console.log(`  → ${incomeList.length} reservation payments seeded\n`);

    // ─── Step 2: Onboarding — Chart of Accounts ──────────────────
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 2: Onboarding — Chart of Accounts');
    console.log('══════════════════════════════════════════════');
    const chartOfAccounts = [
      { accountNumber: '1000', accountName: 'Cash', accountType: 'ASSET' },
      { accountNumber: '1100', accountName: 'Accounts Receivable', accountType: 'ASSET' },
      { accountNumber: '4000', accountName: 'Room Revenue', accountType: 'REVENUE' },
      { accountNumber: '4100', accountName: 'Food & Beverage Revenue', accountType: 'REVENUE' },
      { accountNumber: '4200', accountName: 'Activities Revenue', accountType: 'REVENUE' },
      { accountNumber: '5000', accountName: 'Cost of Goods Sold', accountType: 'EXPENSE' },
      { accountNumber: '5100', accountName: 'Payroll', accountType: 'EXPENSE' },
      { accountNumber: '6000', accountName: 'Utilities', accountType: 'EXPENSE' },
    ];

    const coaRes = await request.post(url('/api/onboarding/chart-of-accounts'), {
      data: chartOfAccounts,
    });
    expect(coaRes.status()).toBe(204);

    // Verify chart of accounts saved
    const coaGet = await request.get(url('/api/onboarding/chart-of-accounts'));
    expect(coaGet.ok()).toBeTruthy();
    const savedCoa = await coaGet.json();
    expect(savedCoa.length).toBe(8);
    for (const a of chartOfAccounts) {
      console.log(`  ✓ ${a.accountNumber} — ${a.accountName} (${a.accountType})`);
    }
    console.log(`  → ${savedCoa.length} accounts saved\n`);

    // ─── Step 3: Onboarding — Products & Services ────────────────
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 3: Onboarding — Products & Services');
    console.log('══════════════════════════════════════════════');
    const products = [
      { name: 'Cabin Rental', type: 'SERVICE', defaultPrice: 250.00, unitOfMeasure: 'per night' },
      { name: 'Suite Rental', type: 'SERVICE', defaultPrice: 450.00, unitOfMeasure: 'per night' },
      { name: 'Restaurant Dining', type: 'SERVICE', defaultPrice: 0, unitOfMeasure: 'per event' },
      { name: 'Guided Fishing Trip', type: 'SERVICE', defaultPrice: 125.00, unitOfMeasure: 'per person' },
      { name: 'Gift Shop Merchandise', type: 'PRODUCT', defaultPrice: 0, unitOfMeasure: 'each' },
    ];

    const psRes = await request.post(url('/api/onboarding/products-services'), {
      data: products,
    });
    expect(psRes.status()).toBe(204);

    // Verify products saved
    const psGet = await request.get(url('/api/onboarding/products-services'));
    expect(psGet.ok()).toBeTruthy();
    const savedPs = await psGet.json();
    expect(savedPs.length).toBe(5);
    for (const p of products) {
      console.log(`  ✓ ${p.name} (${p.type}) — $${p.defaultPrice.toFixed(2)} / ${p.unitOfMeasure}`);
    }
    console.log(`  → ${savedPs.length} products saved\n`);

    // ─── Step 4: Onboarding — Connect bank (Plaid sandbox) ───────
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 4: Connecting bank (Plaid sandbox)');
    console.log('══════════════════════════════════════════════');
    // 4a. Create a sandbox public token (bypasses Link UI for testing)
    console.log('  → Creating sandbox public token...');
    const sandboxRes = await request.post(url('/api/test/plaid-sandbox-token'));
    expect(sandboxRes.ok(), `sandbox token failed: ${sandboxRes.status()}`).toBeTruthy();
    const { publicToken } = await sandboxRes.json();
    expect(publicToken).toBeTruthy();
    console.log(`  ✓ Sandbox token received: ${publicToken.substring(0, 30)}...`);

    // 4b. Exchange the public token for an access token (real Plaid sandbox call)
    console.log('  → Exchanging for access token...');
    const exchangeRes = await request.post(url('/api/plaid/exchange'), {
      data: { publicToken, institutionName: 'First Platypus Bank (Sandbox)' },
    });
    expect(exchangeRes.ok(), `plaid exchange failed: ${exchangeRes.status()}`).toBeTruthy();
    console.log('  ✓ Access token exchanged — institution: First Platypus Bank (Sandbox)');

    // 4c. Verify bank connection is active
    const statusRes = await request.get(url('/api/plaid/status'));
    expect(statusRes.ok()).toBeTruthy();
    const { connected } = await statusRes.json();
    expect(connected).toBe(true);
    console.log(`  ✓ Bank connection active: ${connected}\n`);

    // ─── Step 5: Complete onboarding ──────────────────────────────
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 5: Completing onboarding');
    console.log('══════════════════════════════════════════════');
    const completeRes = await request.post(url('/api/onboarding/complete'));
    expect(completeRes.status()).toBe(204);

    // Verify progress
    const progressRes = await request.get(url('/api/onboarding/progress'));
    expect(progressRes.ok()).toBeTruthy();
    const progress = await progressRes.json();
    expect(progress.chartOfAccountsCreated).toBe(true);
    expect(progress.productsServicesCreated).toBe(true);
    expect(progress.completed).toBe(true);
    console.log(`  ✓ Chart of Accounts: ${progress.chartOfAccountsCreated}`);
    console.log(`  ✓ Products & Services: ${progress.productsServicesCreated}`);
    console.log(`  ✓ Bank Connected: ${progress.bankConnected}`);
    console.log(`  ✓ Onboarding Complete: ${progress.completed}\n`);

    // ─── Step 6: Seed bank transactions (reservation deposits) ───
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 6: Seeding bank deposits (Plaid transactions)');
    console.log('══════════════════════════════════════════════');
    // In production these come from Plaid sync; here we seed them
    // directly so reconciliation has deterministic data to match.
    for (const dep of BANK_DEPOSITS) {
      await seedBankTransaction(request, {
        transactionId: dep.txId,
        amount: dep.amount,
        date: daysAgo(dep.daysAgo),
        name: dep.name,
        merchantName: dep.merchant,
      });
      console.log(`  ✓ ${dep.txId} — ${dep.name}: $${Math.abs(dep.amount).toFixed(2)}`);
    }
    console.log(`  → ${BANK_DEPOSITS.length} bank transactions seeded\n`);

    // ─── Step 7: Run reconciliation ──────────────────────────────
    console.log('══════════════════════════════════════════════');
    console.log('  STEP 7: Running auto-reconciliation');
    console.log('══════════════════════════════════════════════');
    // 7a. Generate auto-match suggestions
    console.log('  → Generating match suggestions...');
    const suggestRes = await request.post(url('/api/reconciliation/suggestions/generate'));
    expect(suggestRes.ok()).toBeTruthy();
    const suggestions = await suggestRes.json();

    // Should find matches for the 5 reservation payments
    // (amounts + dates match exactly → high confidence)
    expect(suggestions.length).toBeGreaterThanOrEqual(5);
    console.log(`  ✓ ${suggestions.length} suggestions generated:`);

    // Verify each reservation payment has a suggestion
    const incomeIds = suggestions
      .filter((s: any) => s.incomeId != null)
      .map((s: any) => s.incomeId);
    expect(incomeIds.length).toBeGreaterThanOrEqual(5);

    // All suggestions should be high confidence (amount exact = 40 + date same day = 30 = 70+)
    for (const s of suggestions.filter((s: any) => s.incomeId != null)) {
      expect(s.confidenceScore).toBeGreaterThanOrEqual(70);
      expect(s.status).toBe('PENDING');
      console.log(`    income #${s.incomeId} ↔ ${s.transactionId}  confidence: ${s.confidenceScore}%`);
    }

    // 7b. Accept all high-confidence matches at once
    console.log('  → Accepting all high-confidence matches...');
    const autoMatchRes = await request.post(url('/api/reconciliation/auto-match'));
    expect(autoMatchRes.ok()).toBeTruthy();
    const { accepted } = await autoMatchRes.json();
    expect(accepted).toBeGreaterThanOrEqual(5);
    console.log(`  ✓ ${accepted} matches accepted\n`);

    // 7c. Verify reconciliation summary
    console.log('══════════════════════════════════════════════');
    console.log('  RESULTS: Reconciliation Summary');
    console.log('══════════════════════════════════════════════');
    const summaryRes = await request.get(url('/api/reconciliation/summary'));
    expect(summaryRes.ok()).toBeTruthy();
    const summary = await summaryRes.json();

    // 5 income entries matched to 5 bank deposits
    expect(summary.matchedCount).toBeGreaterThanOrEqual(5);
    // 1 bank deposit (bank_dep_9999) has no matching income
    expect(summary.unmatchedBankCount).toBeGreaterThanOrEqual(1);
    expect(summary.status).toBe('SUCCESS');
    console.log(`  Matched:             ${summary.matchedCount}`);
    console.log(`  Unmatched payments:  ${summary.unmatchedPaymentCount}`);
    console.log(`  Unmatched bank txns: ${summary.unmatchedBankCount}`);
    console.log(`  Total payments:      ${summary.totalPayments}`);
    console.log(`  Total bank txns:     ${summary.totalBankTransactions}`);
    console.log(`  Status:              ${summary.status}`);

    // 7d. Verify matched income is now flagged as reconciled
    const reconciledIncome = await request.get(url('/api/income'));
    const allIncome = await reconciledIncome.json();
    const reconciled = allIncome.filter((i: any) => i.reconciled === true);
    expect(reconciled.length).toBe(5);
    console.log(`\n  Reconciled income entries: ${reconciled.length}/5`);

    // 7e. Verify income matches exist
    const matchesRes = await request.get(url('/api/reconciliation/matches/income'));
    expect(matchesRes.ok()).toBeTruthy();
    const incomeMatches = await matchesRes.json();
    expect(incomeMatches.length).toBe(5);
    console.log('  Income matches:');
    for (const m of incomeMatches) {
      expect(m.incomeId).toBeTruthy();
      expect(m.transactionId).toBeTruthy();
      expect(m.matchType).toBe('AUTO');
      console.log(`    income #${m.incomeId} ↔ ${m.transactionId} (${m.matchType})`);
    }
    console.log('\n  ✓ ALL ASSERTIONS PASSED');
    console.log('══════════════════════════════════════════════\n');
  });
});
