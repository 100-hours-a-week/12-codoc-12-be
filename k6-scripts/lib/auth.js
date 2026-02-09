import { request } from './http.js';
import { env } from './config.js';
import { checkOk, checkJson } from './checks.js';

export function getToken() {
  if (env.AUTH_MODE === 'token') {
    if (!env.AUTH_TOKEN) throw new Error('AUTH_TOKEN required for token mode');
    return env.AUTH_TOKEN;
  }

  // TODO: 로그인 엔드포인트/페이로드/응답 파싱 수정 필요
  const res = request(
    'POST',
    `${env.BASE_URL}/api/auth/login`,
    JSON.stringify({ username: env.AUTH_USER, password: env.AUTH_PASS }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const ok = checkOk(res, 'login');
  const ok2 = checkJson(res, 'data.accessToken', 'login');
  if (!ok || !ok2) {
    throw new Error(`login failed: ${res.status}`);
  }
  return res.json('data.accessToken');
}
