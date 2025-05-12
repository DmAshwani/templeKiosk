package in.dataman.companyController;

import java.util.Map;

import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;

import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    public ReportHeaderController(ReportHeaderService reportHeaderService) {
        this.reportHeaderService = reportHeaderService;
    }

    @GetMapping("/report-header")
    public ResponseEntity<?> getReportHeader() {
        Map<String, Object> header = reportHeaderService.getReportHeader();

        if (header.isEmpty()) {

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(Map.of("message", "Report header not found"), encryptionDecryptionUtil);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(result);
        }

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(header, encryptionDecryptionUtil);


        return ResponseEntity.ok(result);
    }
}


