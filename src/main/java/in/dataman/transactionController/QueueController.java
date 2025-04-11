package in.dataman.transactionController;

import java.util.Collections;
import java.util.Map;

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

    @PostMapping("/queues")
    public ResponseEntity<?> createQueue(@RequestBody QueueDTO queueDTO) {
        try {

            return ResponseEntity.ok(queueService.createQueue(queueDTO));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/queue-receipt")
    public ResponseEntity<Map<String, Object>> getPrasadBookingDetails(@RequestParam String docId) {
        try {
            Map<String, Object> details = queueService.getQueueDetails(Long.parseLong(docId));
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }
    
}
