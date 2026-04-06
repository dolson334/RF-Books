import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  retries: 0,
  workers: 1,
  use: {
    baseURL: 'http://localhost:8081',
  },
  projects: [
    {
      name: 'api-tests',
      testMatch: '**/*.spec.ts',
      testIgnore: '**/*.ui.spec.ts',
    },
    {
      name: 'ui-tests',
      testMatch: '**/*.ui.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
        baseURL: 'http://localhost:4201',
        headless: false,
        launchOptions: { slowMo: 600 },
        video: 'on',
      },
    },
  ],
});
