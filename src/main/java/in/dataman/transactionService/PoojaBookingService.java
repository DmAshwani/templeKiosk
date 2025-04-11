package in.dataman.transactionService;

import in.dataman.transactionRepo.PoojaBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PoojaBookingService {

    @Autowired
    private PoojaBookingRepository poojaBookingRepository;

    @Autowired
    private ServiceBookingSrv serviceBookingSrv;

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

}
