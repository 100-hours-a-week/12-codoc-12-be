import { env } from './lib/config.js';
import { getToken } from './lib/auth.js';
import { scenarioOptions, thresholds } from './lib/scenarios.js';
import { handleSummary } from './lib/summary.js';

import infraScenario from './scenarios/infra_platform.js';
import dbScenario from './scenarios/deps_db.js';
import aiScenario from './scenarios/deps_ai.js';
import e2eScenario from './scenarios/app_e2e.js';

export const options = {
  ...scenarioOptions(),
  thresholds: thresholds(),
};

export function setup() {
  if (env.SCENARIO === 'infra') {
    return {};
  }
  const token = getToken();
  return { token };
}

export default function (data) {
  if (env.SCENARIO === 'infra') {
    return infraScenario();
  }

  const token = data.token;

  if (env.SCENARIO === 'read' || env.SCENARIO === 'write') {
    return dbScenario(token);
  }

  if (env.SCENARIO === 'ai') {
    return aiScenario(token);
  }

  return e2eScenario(token);
}

export function teardown() {
  // no-op
}

export { handleSummary };
