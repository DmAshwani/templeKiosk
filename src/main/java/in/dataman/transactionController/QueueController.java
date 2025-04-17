package in.dataman.transactionController;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
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

import in.dataman.transactionDTO.QueueDTO;
import in.dataman.transactionService.QueueService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class QueueController {

    @Autowired
    private QueueService queueService;


    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    @PostMapping("/queues")
    public ResponseEntity<?> createQueue(@RequestBody JsonNode payload) {
        try {


            QueueDTO queueDTO = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, QueueDTO.class);

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(queueService.createQueue(queueDTO), encryptionDecryptionUtil);


            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/queue-receipt")
    public ResponseEntity<?> getPrasadBookingDetails(@RequestParam String docId) {
        try {
            Map<String, Object> details = queueService.getQueueDetails(Long.parseLong(docId));

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(details, encryptionDecryptionUtil);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }
    
}
