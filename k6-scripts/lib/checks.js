import { check } from 'k6';

export function checkOk(res, name) {
  return check(res, {
    [`${name} status 200`]: (r) => r.status === 200,
  });
}

export function checkJson(res, path, name) {
  return check(res, {
    [`${name} json exists`]: (r) => Boolean(r.json(path)),
  });
}
