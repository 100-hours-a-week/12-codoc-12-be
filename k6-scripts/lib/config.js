export const env = {
  BASE_URL: __ENV.BASE_URL,
  AUTH_MODE: __ENV.AUTH_MODE || 'token',
  AUTH_TOKEN: __ENV.AUTH_TOKEN || '',
  AUTH_USER: __ENV.AUTH_USER || '',
  AUTH_PASS: __ENV.AUTH_PASS || '',
  MODE: __ENV.MODE || 'smoke',
  SCENARIO: __ENV.SCENARIO || 'read',
  VUS: Number.parseInt(__ENV.VUS || '5', 10),
  DURATION: __ENV.DURATION || '30s',
  AI_ENABLED: __ENV.AI_ENABLED === 'true',
  SSE_ENABLED: __ENV.SSE_ENABLED === 'true',
  SUMMARY_JSON: __ENV.SUMMARY_JSON === 'true',
};

if (!env.BASE_URL) {
  throw new Error('BASE_URL is required');
}
