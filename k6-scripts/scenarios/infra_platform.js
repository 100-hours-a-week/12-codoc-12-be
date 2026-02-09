import { request } from '../lib/http.js';
import { env } from '../lib/config.js';
import { checkOk } from '../lib/checks.js';
import { thinkTime } from '../lib/pacing.js';

export default function () {
  // TODO: infra 목적이면 최소/얕은 엔드포인트로 유지
  const res = request('GET', `${env.BASE_URL}/api/health`, null, {});
  checkOk(res, 'health');

  thinkTime(0.3, 1.0);
}
