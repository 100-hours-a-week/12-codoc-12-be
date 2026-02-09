import { sleep } from 'k6';

export function thinkTime(minSeconds, maxSeconds) {
  const t = Math.random() * (maxSeconds - minSeconds) + minSeconds;
  sleep(t);
}
