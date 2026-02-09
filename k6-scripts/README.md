# k6 부하 테스트 스크립트

이 폴더에는 개발 환경 CD에서 사용하는 k6 스크립트가 있습니다.

## develop-load.js

시나리오(자연스러운 흐름):
1. dev-auth 로그인(필요 시 유저 생성)
2. 헬스 체크 + 프로필 조회
3. 문제 1개 목록 조회 + 상세 조회
4. 요약카드 제출(항상 첫번째 선택지)
5. 요약카드 통과 시 퀴즈 순차 제출(항상 첫번째 선택지)
6. 퀴즈 제출 완료 시 문제 풀이 제출
7. 챗봇 스트리밍 엔드포인트 호출

모든 요청에는 `X-Loadtest: true` 헤더를 포함해 로그 필터링이 가능합니다.

### 환경 변수
- `BASE_URL` (필수): 예) `https://dev-api.example.com`
- `DEV_AUTH_ENDPOINT` (선택): 기본값 `/api/dev/auth/login`
- `VUS` (선택): 기본값 `5`
- `DURATION` (선택): 기본값 `30s`
- `CHATBOT_MESSAGE` (선택): 기본값 `k6 loadtest message`
- `PROBLEM_ID` (선택): 지정 시 해당 문제만 사용 (예: `1`)

### 로컬 실행
```bash
k6 run k6-scripts/develop-load.js \
  -e BASE_URL=https://dev-api.example.com \
  -e VUS=5 \
  -e DURATION=30s
```
