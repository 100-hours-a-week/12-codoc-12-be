import { request } from '../lib/http.js';
import { env } from '../lib/config.js';
import { checkOk } from '../lib/checks.js';
import { thinkTime } from '../lib/pacing.js';

export default function (token) {
  if (env.SCENARIO === 'read') {
    // TODO: read-heavy API로 교체
    const res = request('GET', `${env.BASE_URL}/api/problems?limit=10`, null, {
      headers: { Authorization: `Bearer ${token}` },
    });
    checkOk(res, 'db-read');
    thinkTime(0.5, 1.5);
    return;
  }

  if (env.SCENARIO === 'write') {
    // TODO: write-heavy API로 교체
    const res2 = request('POST', `${env.BASE_URL}/api/summary-cards/submissions`, JSON.stringify({
      problemId: 1,
      choiceIds: [0, 0, 0],
    }), {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    });
    checkOk(res2, 'db-write');
    thinkTime(0.5, 1.5);
    return;
  }

  // default: mixed
  const res = request('GET', `${env.BASE_URL}/api/problems?limit=10`, null, {
    headers: { Authorization: `Bearer ${token}` },
  });
  checkOk(res, 'db-read');

  const res2 = request('POST', `${env.BASE_URL}/api/summary-cards/submissions`, JSON.stringify({
    problemId: 1,
    choiceIds: [0, 0, 0],
  }), {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  });
  checkOk(res2, 'db-write');

  thinkTime(0.5, 1.5);
}
