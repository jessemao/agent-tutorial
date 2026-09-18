import { defineConfig, devices } from '@playwright/test';

const artifactRoot = process.env.PLAYWRIGHT_ARTIFACT_DIR ?? 'test-results/ui-acceptance';

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  outputDir: `${artifactRoot}/results`,
  reporter: [
    ['line'],
    ['html', { outputFolder: `${artifactRoot}/report`, open: 'never' }],
  ],
  use: {
    ...devices['Desktop Chrome'],
    baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:8080',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    launchOptions: { args: ['--no-sandbox'] },
  },
});
