package in.dataman.transactionController;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.dataman.transactionDTO.SafeDepositDTO;
import in.dataman.transactionService.SafeDepositService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class SafeDepositController {

    @Autowired
    private SafeDepositService safeDepositService;

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @PostMapping("/deposite")
    public ResponseEntity<?> createQueue(@RequestBody JsonNode payload) {
        try {

            SafeDepositDTO DTO = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, SafeDepositDTO.class);

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(safeDepositService.createSafeDeposit(DTO) ,encryptionDecryptionUtil);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/locker-receipt")
    public ResponseEntity<?> getPrasadBookingDetails(@RequestParam String docId) {
        try {
            Map<String, Object> details = safeDepositService.getSafeDepositDetails(Long.parseLong(docId));
            System.out.println(details);
            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(details ,encryptionDecryptionUtil);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }
    
}
