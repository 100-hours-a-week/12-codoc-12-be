package _ganzi.codoc.submission.service;

import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.submission.config.ProblemSessionProperties;
import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.enums.ProblemSessionStatus;
import _ganzi.codoc.submission.repository.ProblemSessionRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemSessionService {

    private final ProblemSessionRepository problemSessionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemSessionProperties problemSessionProperties;

    @Transactional
    public ProblemSession resolveOrCreate(Long userId, Long problemId) {
        Instant now = Instant.now();
        ProblemSession activeSession =
                problemSessionRepository
                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                userId, problemId, ProblemSessionStatus.ACTIVE)
                        .orElse(null);

        if (activeSession != null) {
            if (!activeSession.isExpired(now)) {
                return activeSession;
            }
            activeSession.markExpired();
            problemSessionRepository.save(activeSession);
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem =
                problemRepository.findById(problemId).orElseThrow(ProblemNotFoundException::new);

        Instant expiresAt = now.plus(problemSessionProperties.ttl());
        ProblemSession newSession = ProblemSession.create(user, problem, expiresAt);
        return problemSessionRepository.save(newSession);
    }

    @Transactional
    public ProblemSession requireActive(Long userId, Long problemId) {
        Instant now = Instant.now();
        ProblemSession activeSession =
                problemSessionRepository
                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                userId, problemId, ProblemSessionStatus.ACTIVE)
                        .orElse(null);

        if (activeSession == null) {
            return null;
        }

        if (activeSession.isExpired(now)) {
            activeSession.markExpired();
            problemSessionRepository.save(activeSession);
            return null;
        }

        return activeSession;
    }

    public long calculateSolveDurationSec(Long userId, Instant startAt, Instant endAt) {
        if (startAt.isAfter(endAt)) {
            return 0L;
        }

        List<ProblemSession> sessions =
                problemSessionRepository.findOverlappingSessions(userId, startAt, endAt);

        long totalSeconds = 0L;
        for (ProblemSession session : sessions) {
            Instant sessionStart =
                    session.getCreatedAt().isAfter(startAt) ? session.getCreatedAt() : startAt;
            Instant sessionEnd =
                    session.getClosedAt() != null ? session.getClosedAt() : session.getExpiresAt();
            if (sessionEnd.isAfter(endAt)) {
                sessionEnd = endAt;
            }

            if (sessionEnd.isAfter(sessionStart)) {
                totalSeconds += Duration.between(sessionStart, sessionEnd).getSeconds();
            }
        }

        return totalSeconds;
    }
}
