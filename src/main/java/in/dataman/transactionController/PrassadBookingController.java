package in.dataman.transactionController;

import java.util.Collections;
import java.util.List;
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

import in.dataman.transactionDTO.PrasadBookingDTO;
import in.dataman.transactionService.ItemService;
import in.dataman.transactionService.PrasadBookingService;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class PrassadBookingController {

	@Autowired
	private ItemService itemService;

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @GetMapping("/item")
    public ResponseEntity<?> getItems() {
        try {
            List<Map<String, String>> response = itemService.getItems();

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(response ,encryptionDecryptionUtil);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing JSON response");
        }
    }
    
    @Autowired
    private PrasadBookingService prasadBookingService;

//    @PostMapping("/book")
//    public ResponseEntity<Map<String, String>> bookPrasad(@RequestBody JsonNode payload) {
//        try {
//
//            PrasadBookingDTO dto = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, PrasadBookingDTO.class);
//
//            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(prasadBookingService.createPrasadBooking(dto), encryptionDecryptionUtil);
//
//
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("error", e.getMessage()));
//        }
//    }

    @PostMapping("/book")
    public ResponseEntity<Map<String, String>> bookPrasad(@RequestBody JsonNode payload) {
        try {
            //Map<String, String> response = prasadBookingService.createPrasadBooking(dto);

            PrasadBookingDTO dto = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, PrasadBookingDTO.class);

            Map<String, String> response = prasadBookingService.createPrasadBooking(dto);

            System.out.println(response);

            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(response , encryptionDecryptionUtil);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @PostMapping("/cancel-prassad")
    public ResponseEntity<?> cancelPayment(
            @RequestParam String docId,
            @RequestParam String cancelledDt
    ) {
        String result = prasadBookingService.cancelPaymentByDocId(docId, cancelledDt);
        if (result.startsWith("No Record Found")) {
            Map<String, String> resp = PayloadEncryptionDecryptionUtil.encryptResponse(result, encryptionDecryptionUtil);
            return ResponseEntity.badRequest().body(resp);
        }
        Map<String, String> resp = PayloadEncryptionDecryptionUtil.encryptResponse(result, encryptionDecryptionUtil);

        return ResponseEntity.ok(resp);
    }
    
    
    @GetMapping("/prassad-receipt")
    public ResponseEntity<?> getPrasadBookingDetails(@RequestParam String docId) {
        try {
            Map<String, Object> details = prasadBookingService.getPrasadBookingDetails(Long.parseLong(docId));

            Map<String, String> resp = PayloadEncryptionDecryptionUtil.encryptResponse(details, encryptionDecryptionUtil);

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }

}
