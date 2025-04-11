package in.dataman.transactionController;

import java.util.Collections;
import java.util.List;
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

import in.dataman.transactionDTO.PrasadBookingDTO;
import in.dataman.transactionService.ItemService;
import in.dataman.transactionService.PrasadBookingService;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class PrassadBookingController {

	@Autowired
	private ItemService itemService;

    @GetMapping("/item")
    public ResponseEntity<?> getItems() {
        try {
            List<Map<String,Object>> response = itemService.getItems();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing JSON response");
        }
    }
    
    @Autowired
    private PrasadBookingService prasadBookingService;

    @PostMapping("/book")
    public ResponseEntity<Map<String, String>> bookPrasad(@RequestBody PrasadBookingDTO dto) {
        try {
            Map<String, String> response = prasadBookingService.createPrasadBooking(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @PostMapping("/cancel-prassad")
    public ResponseEntity<String> cancelPayment(
            @RequestParam String docId,
            @RequestParam String cancelledDt
    ) {
        String result = prasadBookingService.cancelPaymentByDocId(docId, cancelledDt);
        if (result.startsWith("No Record Found")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
    
    
    @GetMapping("/prassad-receipt")
    public ResponseEntity<Map<String, Object>> getPrasadBookingDetails(@RequestParam String docId) {
        try {
            Map<String, Object> details = prasadBookingService.getPrasadBookingDetails(Long.parseLong(docId));
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }
	
}
