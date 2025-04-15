package in.dataman.transactionService;

import dataman.dmbase.debug.Debug;
import in.dataman.exceptions.BookingException;
import in.dataman.transactionEntity.ServiceBookingDateWiseSummary;
import in.dataman.transactionEntity.ServiceBookingDateWiseSummaryId;
import in.dataman.transactionRepo.PoojaBookingRepository;
import in.dataman.util.BookingUtils;
import in.dataman.util.Util;
import io.lettuce.core.ScriptOutputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PoojaBookingService {

    @Autowired
    private PoojaBookingRepository poojaBookingRepository;

    @Autowired
    private ServiceBookingSrv serviceBookingSrv;


    @Autowired
    @Qualifier("TransactionJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ServiceBookingDateWiseSummarySrv serviceBookingDateWiseSummarySrv;

    @Autowired
    private Util util;

    public List<Map<String, Object>> getPoojaListList(String category){
        return poojaBookingRepository.getPGItems(category);
    }

    public Map<String, Object> getPoojaDetails(Long code){

        Integer perDayQuata = (Integer) poojaBookingRepository.getPoojaDetails(Long.valueOf(code)).get("perDayQuota");
        System.out.println("perDayQuata "+ perDayQuata);
        System.out.println("Date   "+serviceBookingSrv.convertUnixTimestampToDate(String.valueOf(1744270277)));
        //Integer totalBooking = poojaBookingRepository.getTotalBooking(1, 1, serviceBookingSrv.convertUnixTimestampToDate(String.valueOf(1744270277)));

        //System.out.println("Total Pooja Booking "+totalBooking);
        return poojaBookingRepository.getPoojaDetails(code);
    }

//    public List<Map<String, Object>> get15DayBookingSummary(Long itemCode, LocalDate startDate) {
//
//        List<Map<String, Object>> existingData = poojaBookingRepository.getBookingSummaryAfterDate(itemCode, startDate);
//
//        //return existingData;
//        // Create a map for quick lookup by date
//        Map<LocalDate, Map<String, Object>> existingDataMap = existingData.stream()
//                .collect(Collectors.toMap(
//                        row -> ((Timestamp) row.get("vDate")).toLocalDateTime().toLocalDate(),
//                        row -> row,
//                        (existing, replacement) -> existing // In case of duplicate dates, keep the first
//                ));
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        for (int i = 0; i < 15; i++) {
//            LocalDate date = startDate.plusDays(i);
//
//            if (existingDataMap.containsKey(date)) {
//                result.add(existingDataMap.get(date));
//            } else {
//                // Add a default/empty object for missing dates
//                Map<String, Object> emptyDay = new HashMap<>();
//                emptyDay.put("siteCode", null);
//                emptyDay.put("totalBooking", 0);
//                emptyDay.put("itemCode", itemCode);
//                emptyDay.put("vDate", Timestamp.valueOf(date.atStartOfDay())); // or ZonedDateTime as needed
//                emptyDay.put("availability", getPoojaDetails(itemCode).get("perDayQuota"));
//                result.add(emptyDay);
//            }
//        }
//
//        return result;
//    }


    public List<Map<String, Object>> get15DayBookingSummary(Long itemCode, LocalDate startDate) {
        List<Map<String, Object>> existingData = poojaBookingRepository.getBookingSummaryAfterDate(itemCode, startDate);

        // Map with only LocalDate as key, ignoring time
        Map<LocalDate, Map<String, Object>> existingDataMap = existingData.stream()
                .collect(Collectors.toMap(
                        row -> ((Timestamp) row.get("vDate")).toLocalDateTime().toLocalDate(), // Extract only date part
                        row -> {
                            // Override vDate value to just LocalDate string
                            row.put("vDate", ((Timestamp) row.get("vDate")).toLocalDateTime().toLocalDate().toString());
                            return row;
                        },
                        (existing, replacement) -> existing
                ));

        List<Map<String, Object>> result = new ArrayList<>();

        Integer advanceBookingDays = poojaBookingRepository.getAdvanceBookingDaysByCode(String.valueOf(itemCode));

        for (int i = 0; i < advanceBookingDays; i++) {
            LocalDate date = startDate.plusDays(i);

            if (existingDataMap.containsKey(date)) {
                result.add(existingDataMap.get(date));
            } else {
                // Add default row
                Map<String, Object> emptyDay = new HashMap<>();

                emptyDay.put("vDate", date.toString()); // Only date, no time
                emptyDay.put("availability", getPoojaDetails(itemCode).get("perDayQuota"));
                result.add(emptyDay);
            }
        }

        return result;
    }

    public List<LocalDate> excludedDates(LocalDate filterDate, Long itemCode){
        System.out.println("excluded Dates: "+filterDate);
        return poojaBookingRepository.getExclusionDate(filterDate, itemCode);
    }

//===================================================================================================
//    public Map<String, Object> getPujaBookingDetails(Long docId) {
//
//        String sql = """
//             SELECT
//                 sb.recId,
//                 sb.preparedDt,
//                 sb.amount,
//                 ct.cityName,
//                 co.name AS countryName,
//                 sm.name AS stateName,
//                 im.displayName AS puja,
//                 sb.serviceDate,
//                 sb.noOfPerson,
//                 sbd.name AS devoteeName
//             FROM serviceBooking sb
//             LEFT JOIN serviceBookingDetail sbd ON sbd.docId = sb.docId
//             LEFT JOIN itemMast im ON im.code = sb.itemCode
//             LEFT JOIN city ct ON ct.cityCode = sbd.cityCode
//             LEFT JOIN country co ON co.code = ct.countryCode
//             LEFT JOIN stateMast sm ON sm.code = ct.stateCode
//             WHERE sb.docId = ?
//               AND sbd.isMainDevotee = 1
//             """;
//
//        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, docId);
//
//        if (resultList.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        Map<String, Object> firstRow = resultList.get(0);
//        Map<String, Object> response = new HashMap<>();
//
//        String formattedVDate = formatDate(firstRow.get("preparedDt"));
//        String formattedServiceDate = formatDate(firstRow.get("serviceDate"));
//
//        System.out.println("formattedVDate "+formattedVDate);
//        response.put("recId", firstRow.get("recId"));
//        response.put("visitDate", formattedVDate);
////        response.put("visitTime", firstRow.get("v_Time"));
//        response.put("amount", firstRow.get("amount"));
//        response.put("city", firstRow.get("cityName"));
//        response.put("country", firstRow.get("countryName"));
//        response.put("state", firstRow.get("stateName"));
//        response.put("puja", firstRow.get("puja"));
//        response.put("serviceDate", formattedServiceDate);
//        response.put("noOfPerson", firstRow.get("noOfPerson"));
//        response.put("devoteeName", firstRow.get("devoteeName"));
//
//        return response;
//    }
//
//
//    private String formatDate(Object dateObj) {
//        if (dateObj == null) {
//            return null;
//        }
//
//        try {
//            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); // Assuming SQL format
//            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a"); // Includes AM/PM
//            return outputFormat.format(inputFormat.parse(dateObj.toString()));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return dateObj.toString(); // Fallback to raw format in case of error
//        }
//    }



public Map<String, Object> getPujaBookingDetails(Long docId) {

    String sql = """
         SELECT 
             sb.recId,
             sb.preparedDt,
             sb.amount,
             ct.cityName,
             co.name AS countryName,
             sm.name AS stateName,
             im.displayName AS puja,
             sb.serviceDate,
             sb.noOfPerson,
             sbd.name AS devoteeName
         FROM serviceBooking sb
         LEFT JOIN serviceBookingDetail sbd ON sbd.docId = sb.docId
         LEFT JOIN itemMast im ON im.code = sb.itemCode
         LEFT JOIN city ct ON ct.cityCode = sbd.cityCode
         LEFT JOIN country co ON co.code = ct.countryCode
         LEFT JOIN stateMast sm ON sm.code = ct.stateCode
         WHERE sb.docId = ?
           AND sbd.isMainDevotee = 1
         """;

    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, docId);

    if (resultList.isEmpty()) {
        return Collections.emptyMap();
    }

    Map<String, Object> firstRow = resultList.get(0);
    Map<String, Object> response = new HashMap<>();

    String formattedVDate = formatDate(firstRow.get("preparedDt"));
    String formattedServiceDate = formatDate(firstRow.get("serviceDate"));

    response.put("recId", firstRow.get("recId"));
    response.put("visitDate", formattedVDate);
    response.put("amount", firstRow.get("amount"));
    response.put("city", firstRow.get("cityName"));
    response.put("country", firstRow.get("countryName"));
    response.put("state", firstRow.get("stateName"));
    response.put("puja", firstRow.get("puja"));
    response.put("serviceDate", formattedServiceDate);
    response.put("noOfPerson", firstRow.get("noOfPerson"));
    response.put("devoteeName", firstRow.get("devoteeName"));

    return response;
}

    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss.S", // Typical SQL timestamp
                "yyyy-MM-dd HH:mm:ss",   // Without milliseconds
                "yyyy-MM-dd",            // Date only
                "dd/MM/yyyy HH:mm:ss",   // In case it’s manually formatted this way
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm a");
                return outputFormat.format(inputFormat.parse(dateObj.toString()));
            } catch (Exception ignored) {
                // Try next format
            }
        }

        // Fallback to raw string if parsing fails
        return dateObj.toString();
    }



    public void manageServiceBookingStaus(String docId, boolean isDelete) {
        Long docID = Long.valueOf(docId);

        System.out.println("isDelete "+isDelete);

        Optional<Map<String, Object>> data = poojaBookingRepository.getBookingSummaryByDocId(docID);
//        if (data.isEmpty()) {
//            throw new BookingException("Error in fetching summary details");
//        }

        Map<String, Object> bookingSummary = data.get();

        System.out.println(data.get());


        int isStatus = BookingUtils.getInt(bookingSummary.get("isStatus"));
        int perDayQuota = BookingUtils.getInt(bookingSummary.get("mastPerDayQuota"));
        Integer transBooking = BookingUtils.getInt(bookingSummary.get("transBooking"));
        int totalBooking = BookingUtils.getInt(bookingSummary.get("totalBooking"));
        Long itemCode = BookingUtils.getLong(bookingSummary.get("itemCode"));
        String serviceDate = BookingUtils.getString(bookingSummary.get("serviceDate"));
        Integer siteCode = BookingUtils.getInt(bookingSummary.get("site_Code"));
        String cancelledBy = BookingUtils.getString(bookingSummary.get("cancelledBy"));

        System.out.println(cancelledBy);
        System.out.println("transe Booking "+transBooking);


        if (isStatus == 1) {
            throw new BookingException("Booking closed");
        }

        if (isStatus == 2 && !isDelete) {
            throw new BookingException("Slot for the given date is full.");
        }

        if(cancelledBy.trim() != ""){
            throw new BookingException("Booking is discarded!");
        }

        if (isDelete) {
            totalBooking = totalBooking - transBooking;
            System.out.println("isDelete true"+ totalBooking);
        } else {
            totalBooking = totalBooking + transBooking;
        }

        int diff = totalBooking - perDayQuota;

        if (totalBooking > perDayQuota) {
            throw new BookingException("only " +(transBooking-diff)+" seats available");
        } else if (totalBooking == perDayQuota) {
            isStatus = 2;
        } else if (totalBooking < 0) {
            throw new BookingException("Booking overflow by " + diff);
        } else {
            isStatus = 0;
        }

        System.out.println("isStatus "+isStatus);
        System.out.println("site Code "+siteCode);

        // update status and total booking
        ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
        id.setItemCode(itemCode);
        id.setServiceDate(serviceDate);
        id.setSite_Code(Integer.parseInt(util.getSiteCode()));

        ServiceBookingDateWiseSummary summary = new ServiceBookingDateWiseSummary();
        summary.setId(id);
        summary.setTotalBooking(totalBooking);
        summary.setIsStatus(isStatus);
        summary.setPerDayQuota(perDayQuota);

        serviceBookingDateWiseSummarySrv.saveDateWiseSummary(summary);
    }
}
