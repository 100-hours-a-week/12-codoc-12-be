import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;
const DEV_AUTH_ENDPOINT = __ENV.DEV_AUTH_ENDPOINT || '/api/dev/auth/login';
const VUS = Number.parseInt(__ENV.VUS || '5', 10);
const DURATION = __ENV.DURATION || '30s';
const CHATBOT_MESSAGE = __ENV.CHATBOT_MESSAGE || 'k6 loadtest message';

if (!BASE_URL) {
  throw new Error('BASE_URL is required. Example: https://dev-api.example.com');
}

export const options = {
  vus: VUS,
  duration: DURATION,
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

export function setup() {
  const nickname = `k6-${Math.random().toString(36).slice(2, 10)}`;
  const res = http.post(
    `${BASE_URL}${DEV_AUTH_ENDPOINT}`,
    JSON.stringify({ nickname }),
    { headers: { 'Content-Type': 'application/json', 'X-Loadtest': 'true' } },
  );

  const ok = check(res, {
    'dev auth status 200': (r) => r.status === 200,
    'dev auth token exists': (r) => Boolean(r.json('data.accessToken')),
  });

  if (!ok) {
    throw new Error(`dev auth failed: ${res.status} ${res.body}`);
  }

  return { token: res.json('data.accessToken') };
}

export default function (data) {
  const authHeaders = {
    Authorization: `Bearer ${data.token}`,
    'X-Loadtest': 'true',
  };

  const healthRes = http.get(`${BASE_URL}/api/health`, {
    headers: { 'X-Loadtest': 'true' },
  });
  check(healthRes, { 'health status 200': (r) => r.status === 200 });

  const profileRes = http.get(`${BASE_URL}/api/user/profile`, { headers: authHeaders });
  check(profileRes, { 'profile status 200': (r) => r.status === 200 });

  const listRes = http.get(`${BASE_URL}/api/problems?limit=1`, { headers: authHeaders });
  const listOk = check(listRes, { 'problem list 200': (r) => r.status === 200 });
  if (!listOk) {
    sleep(1);
    return;
  }

  const problemId = listRes.json('data.items.0.problemId');
  if (!problemId) {
    sleep(1);
    return;
  }

  const detailRes = http.get(`${BASE_URL}/api/problems/${problemId}`, { headers: authHeaders });
  const detailOk = check(detailRes, { 'problem detail 200': (r) => r.status === 200 });
  if (!detailOk) {
    sleep(1);
    return;
  }

  const summaryCards = detailRes.json('data.summaryCards') || [];
  const quizzes = detailRes.json('data.quizzes') || [];

  if (summaryCards.length > 0) {
    const summaryChoiceIds = summaryCards.map(() => 0);
    const summaryRes = http.post(
      `${BASE_URL}/api/summary-cards/submissions`,
      JSON.stringify({ problemId, choiceIds: summaryChoiceIds }),
      { headers: { ...authHeaders, 'Content-Type': 'application/json' } },
    );
    check(summaryRes, { 'summary submit 200': (r) => r.status === 200 });
  }

  if (quizzes.length > 0) {
    let attemptId = null;
    for (const quiz of quizzes) {
      const quizRes = http.post(
        `${BASE_URL}/api/quizzes/${quiz.quizId}/submissions`,
        JSON.stringify({
          choiceId: 0,
          idempotencyKey: `k6-${Math.random().toString(36).slice(2, 10)}`,
          attemptId,
        }),
        { headers: { ...authHeaders, 'Content-Type': 'application/json' } },
      );
      const quizOk = check(quizRes, { 'quiz submit 200': (r) => r.status === 200 });
      if (!quizOk) {
        break;
      }
      attemptId = quizRes.json('data.attemptId');
    }
  }

  const problemRes = http.post(`${BASE_URL}/api/problems/${problemId}/submissions`, null, {
    headers: authHeaders,
  });
  check(problemRes, { 'problem submit 200': (r) => r.status === 200 });

  const chatbotRes = http.post(
    `${BASE_URL}/api/chatbot/messages/stream`,
    JSON.stringify({ problemId, message: CHATBOT_MESSAGE }),
    {
      headers: {
        ...authHeaders,
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
      timeout: '30s',
    },
  );
  check(chatbotRes, { 'chatbot stream 200': (r) => r.status === 200 });

  sleep(1);
}
