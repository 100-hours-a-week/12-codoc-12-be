package _ganzi.codoc.analysis.api;

import _ganzi.codoc.analysis.service.AnalysisReportService;
import _ganzi.codoc.global.dto.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/analysis-reports")
public class AnalysisReportTestController {

    private final AnalysisReportService analysisReportService;

    @PostMapping("/issue-weekly")
    public ResponseEntity<ApiResponse<Map<String, String>>> issueWeeklyReports() {
        analysisReportService.issueWeeklyReports();
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "triggered")));
    }
}
