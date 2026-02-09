import { request } from '../lib/http.js';
import { env } from '../lib/config.js';
import { checkOk } from '../lib/checks.js';
import { thinkTime } from '../lib/pacing.js';

export default function (token) {
  if (!env.AI_ENABLED) return;

  // TODO: AI 경로에 맞는 endpoint/payload로 교체
  const res = request('POST', `${env.BASE_URL}/api/chatbot/messages/stream`, JSON.stringify({
    problemId: 1,
    message: 'k6 ai-heavy',
  }), {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
  });
  checkOk(res, 'ai-stream');

  thinkTime(1.0, 2.0);
}
