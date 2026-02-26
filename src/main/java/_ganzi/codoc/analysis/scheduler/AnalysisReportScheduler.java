package _ganzi.codoc.analysis.scheduler;

import _ganzi.codoc.analysis.service.AnalysisReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisReportScheduler {

    private final AnalysisReportService analysisReportService;

    @Scheduled(cron = "0 0 5 * * MON", zone = "Asia/Seoul")
    public void issueWeeklyReports() {
        analysisReportService.issueWeeklyReports();
    }
}
