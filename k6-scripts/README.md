# codoc loadtest

목표
- CD와 분리된 부하테스트 워크플로우
- Smoke/Step/Spike/E2E 시나리오로 변곡점과 병목 탐지
- AI 경로는 분리하여 비용/지연 변동을 통제

주의
- 이 레포는 GitHub Actions(GitHub-hosted runner)에서 실행을 전제로 한다.
- Soak(장시간) 테스트는 별도 로드 제너레이터 인스턴스에서 실행하는 것이 안정적이다.

실행 방법
1. env/.env.example 참고하여 시크릿 설정
2. GitHub Actions workflow_dispatch로 실행
3. 기본 엔트리포인트는 k6-scripts/run.js

환경 변수
- BASE_URL: 대상 서버 베이스 URL 예: https://dev.codoc.cloud
- AUTH_MODE: token 또는 login
- AUTH_TOKEN: 토큰 방식일 때 사용
- AUTH_USER / AUTH_PASS: 로그인 방식일 때 사용
- MODE: smoke, step, spike, e2e
- SCENARIO: read, write, ai, e2e, infra
- VUS: 기본 동시 사용자 수
- DURATION: 기본 실행 시간
- AI_ENABLED: true/false (SSE 포함 AI 경로 제어)
- SSE_ENABLED: true/false (SSE 경로 분리 제어)
- SUMMARY_JSON: true/false (JSON summary 저장 여부)

TODO 안내
- scenarios/*.js의 API 경로/페이로드를 서비스에 맞게 수정
- lib/auth.js의 로그인 경로/응답 파싱 수정

Soak는 왜 Actions 대신 별도 인스턴스인가
- Actions 러너는 실행 시간 제한과 네트워크 안정성이 낮아 장시간 테스트에 부적합
- 장시간 실행 시 rate-limit, job timeout, 네트워크 단절 위험이 커짐
- 안정적인 수집을 위해 별도 로드 제너레이터(EC2 등)가 적합

GitHub Actions 설정
- 이 워크플로우는 dev 환경만 허용한다.
- 필요한 시크릿
  - BASE_URL_DEV
  - AUTH_MODE
  - AUTH_TOKEN 또는 AUTH_USER / AUTH_PASS
