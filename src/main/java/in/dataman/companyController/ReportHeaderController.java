package in.dataman.companyController;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.dataman.companyService.ReportHeaderService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class ReportHeaderController {

    private final ReportHeaderService reportHeaderService;


    public ReportHeaderController(ReportHeaderService reportHeaderService) {
        this.reportHeaderService = reportHeaderService;
    }

    @GetMapping("/report-header")
    public ResponseEntity<Map<String, Object>> getReportHeader() {
        Map<String, Object> header = reportHeaderService.getReportHeader();

        if (header.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Report header not found"));
        }

        return ResponseEntity.ok(header);
    }
}


