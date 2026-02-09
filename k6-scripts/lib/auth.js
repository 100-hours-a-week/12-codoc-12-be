import { request } from './http.js';
import { env } from './config.js';
import { checkOk, checkJson } from './checks.js';

export function getToken() {
  if (env.AUTH_MODE === 'token') {
    if (!env.AUTH_TOKEN) throw new Error('AUTH_TOKEN required for token mode');
    return env.AUTH_TOKEN;
  }

  if (env.AUTH_MODE === 'dev_auth') {
    const nickname = `k6-${Math.random().toString(36).slice(2, 10)}`;
    const res = request(
      'POST',
      `${env.BASE_URL}${env.DEV_AUTH_ENDPOINT}`,
      JSON.stringify({ nickname }),
      { headers: { 'Content-Type': 'application/json', 'X-Loadtest': 'true' } }
    );
    const ok = checkOk(res, 'dev-auth');
    const ok2 = checkJson(res, 'data.accessToken', 'dev-auth');
    if (!ok || !ok2) {
      throw new Error(`dev-auth failed: ${res.status}`);
    }
    return res.json('data.accessToken');
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

export function getTokens(count) {
  const tokens = [];
  for (let i = 0; i < count; i += 1) {
    tokens.push(getToken());
  }
  return tokens;
}
