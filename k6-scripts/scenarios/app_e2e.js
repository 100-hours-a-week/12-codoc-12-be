import { request } from '../lib/http.js';
import { env } from '../lib/config.js';
import { checkOk } from '../lib/checks.js';
import { thinkTime } from '../lib/pacing.js';

export default function (token) {
  // TODO: 실제 서비스 흐름에 맞게 경로/페이로드 수정
  const list = request('GET', `${env.BASE_URL}/api/problems?limit=1`, null, {
    headers: { Authorization: `Bearer ${token}` },
  });
  checkOk(list, 'problem-list');

  const problemId = list.json('data.items.0.problemId');
  if (!problemId) return;

  const detail = request('GET', `${env.BASE_URL}/api/problems/${problemId}`, null, {
    headers: { Authorization: `Bearer ${token}` },
  });
  checkOk(detail, 'problem-detail');

  if (env.AI_ENABLED && env.SSE_ENABLED) {
    const chat = request('POST', `${env.BASE_URL}/api/chatbot/messages/stream`, JSON.stringify({
      problemId,
      message: 'k6 e2e message',
    }), {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
      },
    });
    checkOk(chat, 'chatbot-stream');
  }

  const summary = request('POST', `${env.BASE_URL}/api/summary-cards/submissions`, JSON.stringify({
    problemId,
    choiceIds: [0, 0, 0],
  }), {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  });
  checkOk(summary, 'summary-submit');

  thinkTime(0.5, 1.5);
}
