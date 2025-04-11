package in.dataman.transactionController;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;

import in.dataman.transactionDTO.DonationDTO;
import in.dataman.transactionService.DonationService;
import in.dataman.transactionService.RazorpayService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class DonationController {

    private static final Logger logger = LoggerFactory.getLogger(DonationController.class);
    private final DonationService donationService;
    
    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private RazorpayClient razorpayClient;
    

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/donations")
    public ResponseEntity<?> createDonation(@RequestBody DonationDTO donationDTO) {
        try {
            Map<String, String> response = donationService.donation(donationDTO);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Donation processing failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Donation processing failed"));
        }
    }
    
    
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestParam String orderId) {
        Map<String, String> response = razorpayService.verifyPayment(orderId);
        
        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    

    @PostMapping("/cancel-donation")
    public ResponseEntity<String> cancelPayment(
            @RequestParam String docId,
            @RequestParam String cancelledDt
    ) {
        String result = donationService.cancelPaymentByDocId(docId, cancelledDt);
        if (result.startsWith("No Record Found")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
    
    
    
    @GetMapping("/payment-status")
    public ResponseEntity<Map<String, String>> getPaymentStatus(@RequestParam String orderId) {
        try {
        	
        	Order order = razorpayClient.orders.fetch(orderId);
        	System.out.println(order);

            // ✅ Fetch payment details from Razorpay using orderId
            List<Payment> payments = razorpayClient.orders.fetchPayments(orderId);//
            System.out.println(payments); //[]
            if (payments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No payment found for this order"));
            }

            // ✅ Fetch the first (latest) payment
            Payment payment = payments.get(0);
            String paymentId = payment.get("id");
            String status = payment.get("status");

            return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "status", status
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Payment status check failed"));
        }
    }

    @GetMapping("/donation-recpte")
    public ResponseEntity<?> getDonations(@RequestParam String docId) {
        try {
        	Map<String, Object> details = donationService.getDonations(Long.parseLong(docId));  // Service layer call
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error processing JSON response");
        }
    }
    
    
}
