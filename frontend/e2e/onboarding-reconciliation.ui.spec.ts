import { test, expect } from '@playwright/test';

const API = 'http://localhost:8081';
const ALIAS = 'testresort';

function apiUrl(path: string): string {
  const sep = path.includes('?') ? '&' : '?';
  return `${API}${path}${sep}resortAlias=${ALIAS}`;
}

function daysAgo(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

// ──────────────────────────────────────────────
//  Demo data — reservation payments & bank deposits
// ──────────────────────────────────────────────

const RESERVATION_PAYMENTS = [
  { guest: 'Anderson Family',         category: 'room_revenue',  amount: 1200, daysAgo: 1, ref: 'RES-1001', desc: 'Cabin 7 – 3 night stay',              pay: 'card' },
  { guest: 'Martinez Wedding Party',  category: 'food_beverage', amount: 4500, daysAgo: 2, ref: 'RES-1002', desc: 'Reception dinner & bar package',       pay: 'ach'  },
  { guest: 'Chen Family',             category: 'room_revenue',  amount: 2800, daysAgo: 3, ref: 'RES-1003', desc: 'Lake Suite – 5 night stay',            pay: 'card' },
  { guest: 'Thompson Group',          category: 'activities',    amount: 750,  daysAgo: 4, ref: 'RES-1004', desc: 'Guided fishing trip x6 guests',        pay: 'card' },
  { guest: 'Davis Retreat',           category: 'room_revenue',  amount: 3600, daysAgo: 5, ref: 'RES-1005', desc: 'Executive Lodge – corporate retreat',  pay: 'ach'  },
];

const BANK_DEPOSITS = [
  { txId: 'bank_dep_1001', amount: -1200, daysAgo: 1, name: 'POS Deposit - Anderson',       merchant: 'Card Payment' },
  { txId: 'bank_dep_1002', amount: -4500, daysAgo: 2, name: 'ACH Credit - Martinez Wedding', merchant: 'ACH Deposit'  },
  { txId: 'bank_dep_1003', amount: -2800, daysAgo: 3, name: 'POS Deposit - Chen',            merchant: 'Card Payment' },
  { txId: 'bank_dep_1004', amount: -750,  daysAgo: 4, name: 'POS Deposit - Thompson',        merchant: 'Card Payment' },
  { txId: 'bank_dep_1005', amount: -3600, daysAgo: 5, name: 'ACH Credit - Davis Corp',       merchant: 'ACH Deposit'  },
  { txId: 'bank_dep_9999', amount: -999,  daysAgo: 2, name: 'Unknown ACH Credit',            merchant: 'Unknown'      },
];

// ──────────────────────────────────────────────
//  Browser-based E2E test
// ──────────────────────────────────────────────

test.describe('Onboarding → Reconciliation (UI)', () => {
  test.beforeEach(async ({ request }) => {
    await request.post(apiUrl('/api/test/cleanup'));
  });

  test.afterAll(async ({ request }) => {
    await request.post(apiUrl('/api/test/cleanup'));
  });

  test('full visual flow', async ({ page, request }) => {
    // ═══════════════════════════════════════════
    //  PHASE 1 — Seed reservation payments via API
    // ═══════════════════════════════════════════
    for (const pmt of RESERVATION_PAYMENTS) {
      await request.post(apiUrl('/api/income'), {
        data: {
          incomeDate: daysAgo(pmt.daysAgo),
          amount: pmt.amount,
          source: pmt.guest,
          category: pmt.category,
          paymentMethod: pmt.pay,
          referenceNumber: pmt.ref,
          description: pmt.desc,
        },
      });
    }

    // ═══════════════════════════════════════════
    //  PHASE 2 — Onboarding Wizard (in the browser)
    // ═══════════════════════════════════════════
    await page.goto('/onboarding');
    await expect(page.locator('h1')).toContainText('Setup Your Books');

    // Step 1: Bank — skip for now (Plaid Link can't be automated in browser)
    await page.getByText('Skip for now →').click();

    // Step 2: Chart of Accounts — see pre-loaded accounts, then save
    await expect(page.locator('h2')).toContainText('Chart of Accounts');
    // Wait for the pre-loaded accounts table to appear
    await expect(page.locator('.data-table tbody tr').first()).toBeVisible();
    // Click "Save & Continue"
    await page.getByRole('button', { name: 'Save & Continue →' }).click();

    // Step 3: Products & Services — add a product, then save
    await expect(page.locator('h2')).toContainText('Products & Services');
    // Add a product via the inline form
    await page.locator('input[placeholder="Name"]').fill('Cabin Rental');
    await page.locator('select').selectOption('SERVICE');
    await page.locator('input[placeholder="Price"]').fill('250');
    await page.locator('input[placeholder="Unit (e.g., night)"]').fill('per night');
    await page.getByRole('button', { name: '+ Add' }).click();
    // Verify it appeared in the table
    await expect(page.locator('.data-table')).toContainText('Cabin Rental');
    // Save & continue
    await page.getByRole('button', { name: 'Save & Continue →' }).click();

    // Step 4: Complete — verify summary
    await expect(page.locator('h2')).toContainText("You're All Set!");
    await expect(page.locator('.summary-item').nth(1)).toContainText('Chart of Accounts');
    await expect(page.locator('.summary-item').nth(2)).toContainText('Products & Services');

    // Connect Plaid via API (sandbox) before finishing
    const sandboxRes = await request.post(apiUrl('/api/test/plaid-sandbox-token'));
    const { publicToken } = await sandboxRes.json();
    await request.post(apiUrl('/api/plaid/exchange'), {
      data: { publicToken, institutionName: 'First Platypus Bank (Sandbox)' },
    });

    // Go to dashboard
    await page.getByRole('button', { name: 'Go to Dashboard →' }).click();
    await expect(page.locator('h1')).toContainText('RF Books');

    // ═══════════════════════════════════════════
    //  PHASE 3 — Seed bank deposits via API
    // ═══════════════════════════════════════════
    for (const dep of BANK_DEPOSITS) {
      await request.post(apiUrl('/api/test/seed-bank-transaction'), {
        data: {
          transactionId: dep.txId,
          amount: dep.amount,
          date: daysAgo(dep.daysAgo),
          name: dep.name,
          merchantName: dep.merchant,
        },
      });
    }

    // ═══════════════════════════════════════════
    //  PHASE 4 — View income page
    // ═══════════════════════════════════════════
    await page.getByRole('link', { name: 'Income' }).click();
    await expect(page.locator('h1')).toContainText('Income');
    // Wait for income table to load with our 5 reservations
    await expect(page.locator('table tbody tr')).toHaveCount(5, { timeout: 10000 });
    await page.waitForTimeout(1500); // Let user see the income list

    // ═══════════════════════════════════════════
    //  PHASE 5 — Reconciliation  
    // ═══════════════════════════════════════════
    await page.getByRole('link', { name: 'Reconciliation' }).click();
    await expect(page.locator('h1')).toContainText('Reconciliation Center');

    // Wait for the page to load data
    await page.waitForTimeout(1500);

    // Click "Refresh Data" to load bank transactions
    await page.getByRole('button', { name: 'Refresh Data' }).click();
    await page.waitForTimeout(1500);

    // Should see unreconciled income
    const incomeTab = page.getByRole('button', { name: /Income/ });
    await incomeTab.click();
    await page.waitForTimeout(1000);

    // Click "Smart Match" to generate suggestions
    await page.getByRole('button', { name: /Smart Match/ }).click();
    // Wait for suggestions panel to appear
    await expect(page.locator('.suggestions-panel')).toBeVisible({ timeout: 10000 });
    await page.waitForTimeout(2000); // Let user see the suggestions

    // Verify suggestion badges show confidence scores
    const badges = page.locator('.confidence-badge');
    await expect(badges.first()).toBeVisible();

    // Click "Accept All" to accept all suggestions
    await page.getByRole('button', { name: 'Accept All' }).click();
    await page.waitForTimeout(2000); // Let user see results

    // Suggestions panel should disappear after accepting all
    await expect(page.locator('.suggestions-panel')).toBeHidden({ timeout: 10000 });

    // Check summary cards — matched count should be 5
    await expect(page.locator('.summary-card.matched .card-value')).toContainText('5');
    await page.waitForTimeout(1000);

    // Switch to "Matched" view to see reconciled income
    await page.getByRole('button', { name: /^Matched/ }).click();
    await page.waitForTimeout(1500);

    // Verify 5 reconciled income entries are shown
    const matchedRows = page.locator('.data-table tbody tr');
    await expect(matchedRows).toHaveCount(5, { timeout: 10000 });

    // ═══════════════════════════════════════════
    //  PHASE 6 — Dashboard shows reconciliation status
    // ═══════════════════════════════════════════
    await page.getByRole('link', { name: 'Dashboard' }).click();
    await expect(page.locator('h1')).toContainText('RF Books');
    await page.waitForTimeout(2000); // Let user see the dashboard

    // Final assertions via API
    const summaryRes = await request.get(apiUrl('/api/reconciliation/summary'));
    const summary = await summaryRes.json();
    expect(summary.matchedCount).toBeGreaterThanOrEqual(5);
    expect(summary.unmatchedBankCount).toBeGreaterThanOrEqual(1);
    expect(summary.status).toBe('SUCCESS');
  });
});
