import { env } from './lib/config.js';
import { scenarioOptions, thresholds } from './lib/scenarios.js';
import { handleSummary } from './lib/summary.js';
import infraScenario from './scenarios/infra_platform.js';

export const options = {
  ...scenarioOptions(),
  thresholds: thresholds(),
};

export default function () {
  return infraScenario();
}

export { handleSummary };
