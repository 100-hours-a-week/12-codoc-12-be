import { env } from './config.js';

export function scenarioOptions() {
  const base = { vus: env.VUS, duration: env.DURATION };

  if (env.MODE === 'smoke') return base;

  if (env.MODE === 'step') {
    return {
      stages: [
        { duration: '1m', target: Math.max(5, env.VUS) },
        { duration: '1m', target: Math.max(10, env.VUS * 2) },
        { duration: '1m', target: Math.max(20, env.VUS * 3) },
        { duration: '1m', target: Math.max(30, env.VUS * 4) },
        { duration: '1m', target: Math.max(40, env.VUS * 5) },
      ],
    };
  }

  if (env.MODE === 'spike') {
    return {
      stages: [
        { duration: '15s', target: Math.max(5, env.VUS) },
        { duration: '15s', target: Math.max(50, env.VUS * 5) },
        { duration: '15s', target: Math.max(5, env.VUS) },
      ],
    };
  }

  if (env.MODE === 'e2e') return base;

  return base;
}

export function thresholds() {
  return {};
}
