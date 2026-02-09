import http from 'k6/http';

export function request(method, url, body, params) {
  const res = http.request(method, url, body, params);
  return res;
}
