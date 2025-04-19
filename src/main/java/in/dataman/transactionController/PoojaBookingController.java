package in.dataman.transactionController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import in.dataman.Enums.VoucherCategory;
import in.dataman.transactionRepo.PoojaBookingRepository;
import in.dataman.transactionService.CommonPoojaBookingService;
import in.dataman.transactionService.ItemService;

import in.dataman.transactionService.PoojaBookingService;
import in.dataman.util.EncryptionDecryptionUtilityClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class PoojaBookingController {

    @Autowired
    private PoojaBookingService poojaBookingService;

    @Autowired
    private PoojaBookingRepository poojaBookingRepository;

    @Autowired
    @Qualifier("TransactionJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommonPoojaBookingService commonPoojaBookingService;

    @Autowired
    private EncryptionDecryptionUtilityClass encryptionDecryptionUtilityClass;

    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    private final ReentrantLock reentrantLock = new ReentrantLock(true);

    @GetMapping("/get-credentials")
    public ResponseEntity<?> getCredentials(){
        return ResponseEntity.ok(encryptionDecryptionUtilityClass.getCredentials());
    }



    @GetMapping("/get-gender")
    public List<Map<String, Object>> getAdministrativeSex() {
        String sql = "SELECT code, name FROM hl7Mast WHERE tableName = 'administrativeSex' AND isActive = 1";
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/get-pooja-list")
    public ResponseEntity<?> getPoojaList(@RequestParam String category){


        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(poojaBookingService.getPoojaListList(category),encryptionDecryptionUtil);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-pooja-details")
    public ResponseEntity<?> getPoojaDetails(){

        return ResponseEntity.ok(poojaBookingService.getPoojaDetails(1l));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getBookingSummary(
            @RequestParam Long itemCode,
            @RequestParam Long startDate // Unix timestamp in milliseconds
    ) {
        // Convert Unix timestamp to LocalDate without using ZoneId
        LocalDate localDate = Instant.ofEpochMilli(startDate*1000)
                .atOffset(ZoneOffset.UTC)  // Use UTC or another fixed offset instead of ZoneId
                .toLocalDate();

        List<Map<String, Object>> summary = poojaBookingService.get15DayBookingSummary(itemCode, localDate);
        List<LocalDate> excludedDate = poojaBookingService.excludedDates(localDate, itemCode);
        System.out.println(excludedDate);

        //return ResponseEntity.ok(summary);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode responseNode = mapper.createObjectNode();

        responseNode.putPOJO("summary", summary);
        responseNode.putPOJO("excludedDates", excludedDate);


        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(responseNode,encryptionDecryptionUtil);
        return ResponseEntity.ok(result);

    }

//    @PostMapping("/book-pooja")
//    public ResponseEntity<?> savePoojaBooking(@RequestBody JsonNode payload, @RequestParam String category) throws Exception {
//
//        System.out.println(VoucherCategory.PUJA_BOOKING);
//        System.out.println(VoucherCategory.TRUSTEE_PUJA_BOOKING);
//
//        String poojaBooking = String.valueOf(VoucherCategory.PUJA_BOOKING);
//
//        if(category.equals(poojaBooking)){
//            System.out.println("Category Pooja Booking");
//        }else{
//            System.out.println("Category trustee Pooja Booking");
//        }
//        JsonNode jsonNode = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, JsonNode.class);
//
//        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(commonPoojaBookingService.bookPooja(jsonNode, category),encryptionDecryptionUtil);
//
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/book-pooja")
    public ResponseEntity<?> savePoojaBooking(@RequestBody JsonNode payload, @RequestParam String category) throws Exception {

        reentrantLock.lock();

        try {
            System.out.println(VoucherCategory.PUJA_BOOKING);
            System.out.println(VoucherCategory.TRUSTEE_PUJA_BOOKING);

            String poojaBooking = String.valueOf(VoucherCategory.PUJA_BOOKING);

            if (category.equals(poojaBooking)) {
                System.out.println("Category Pooja Booking");
            } else {
                System.out.println("Category Trustee Pooja Booking");
            }

            // Decrypt request payload
            JsonNode jsonNode = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, JsonNode.class);

            // Process booking and encrypt the response
            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(
                    commonPoojaBookingService.bookPooja(jsonNode, category),
                    encryptionDecryptionUtil
            );

            return ResponseEntity.ok(result);

        } finally {
            reentrantLock.unlock();
        }
    }


///================================================================================================================


//=========================================================================================================================
    @PostMapping("/cancel-booking")
    public ResponseEntity<?> cancelBooking(@RequestBody JsonNode payload) throws JsonProcessingException {
        JsonNode jsonNode = PayloadEncryptionDecryptionUtil.decryptAndConvertToDTO(payload, encryptionDecryptionUtil, JsonNode.class);

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(commonPoojaBookingService.cancelBooking(jsonNode),encryptionDecryptionUtil);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/booking-receipt")
    public ResponseEntity<?> getBookingDetails(@RequestParam String docId) {
        Long docIds = Long.parseLong(docId);
        Map<String, Object> details = poojaBookingService.getPujaBookingDetails(docIds);

        if (details.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Booking not found"));
        }

        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(details, encryptionDecryptionUtil);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-credential")
    public ResponseEntity<?> getCredential(){
        return ResponseEntity.ok(encryptionDecryptionUtilityClass.getCredentials());
    }


}
