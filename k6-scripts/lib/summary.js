import { env } from './config.js';

export function handleSummary(data) {
  const out = {};
  if (env.SUMMARY_JSON) {
    out['summary.json'] = JSON.stringify(data, null, 2);
  }
  return out;
}
