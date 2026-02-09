import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;
const DEV_AUTH_ENDPOINT = __ENV.DEV_AUTH_ENDPOINT || '/api/dev/auth/login';
const VUS = Number.parseInt(__ENV.VUS || '5', 10);
const DURATION = __ENV.DURATION || '30s';
const ENABLE_THRESHOLDS = __ENV.ENABLE_THRESHOLDS === 'true';
const CHATBOT_MESSAGE = __ENV.CHATBOT_MESSAGE || 'k6 loadtest message';
const PROBLEM_ID = __ENV.PROBLEM_ID ? Number.parseInt(__ENV.PROBLEM_ID, 10) : null;

if (!BASE_URL) {
  throw new Error('BASE_URL is required. Example: https://dev-api.example.com');
}

export const options = {
  vus: VUS,
  duration: DURATION,
};

if (ENABLE_THRESHOLDS) {
  options.thresholds = {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  };
}

export function setup() {
  const tokens = [];
  for (let i = 0; i < VUS; i += 1) {
    const nickname = `k6-${Math.random().toString(36).slice(2, 10)}-${i + 1}`;
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

    tokens.push(res.json('data.accessToken'));
  }
  return { tokens };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const authHeaders = {
    Authorization: `Bearer ${token}`,
    'X-Loadtest': 'true',
  };

  const healthRes = http.get(`${BASE_URL}/api/health`, {
    headers: { 'X-Loadtest': 'true' },
  });
  check(healthRes, { 'health status 200': (r) => r.status === 200 });

  const profileRes = http.get(`${BASE_URL}/api/user/profile`, { headers: authHeaders });
  check(profileRes, { 'profile status 200': (r) => r.status === 200 });

  let problemId = PROBLEM_ID;
  if (!problemId) {
    const listRes = http.get(`${BASE_URL}/api/problems?limit=1`, { headers: authHeaders });
    const listOk = check(listRes, { 'problem list 200': (r) => r.status === 200 });
    if (!listOk) {
      sleep(1);
      return;
    }
    problemId = listRes.json('data.items.0.problemId');
  }
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
  let summaryPassed = summaryCards.length === 0;

  if (summaryCards.length > 0) {
    const summaryChoiceIds = summaryCards.map(() => 0);
    const summaryRes = http.post(
      `${BASE_URL}/api/summary-cards/submissions`,
      JSON.stringify({ problemId, choiceIds: summaryChoiceIds }),
      { headers: { ...authHeaders, 'Content-Type': 'application/json' } },
    );
    const summaryOk = check(summaryRes, { 'summary submit 200': (r) => r.status === 200 });
    if (summaryOk) {
      const status = summaryRes.json('data.status');
      summaryPassed = status === 'SUMMARY_CARD_PASSED' || status === 'SOLVED';
    }
  }

  if (summaryPassed && quizzes.length > 0) {
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

  if (summaryPassed && quizzes.length > 0) {
    const problemRes = http.post(`${BASE_URL}/api/problems/${problemId}/submissions`, null, {
      headers: authHeaders,
    });
    check(problemRes, { 'problem submit 200': (r) => r.status === 200 });
  }

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
