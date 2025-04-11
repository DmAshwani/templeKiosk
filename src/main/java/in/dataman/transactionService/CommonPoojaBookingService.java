package in.dataman.transactionService;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.debug.Debug;
import in.dataman.transactionEntity.*;
import in.dataman.transactionRepo.PaymentDetailRepository;
import in.dataman.transactionRepo.PoojaBookingRepository;
import in.dataman.transactionRepo.ServiceBookingRepository;
import in.dataman.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CommonPoojaBookingService {


    @Autowired
    private ServiceBookingDetailSrv serviceBookingDetailSrv;

    @Autowired
    private ServiceBookingSrv serviceBookingSrv;

    @Autowired
    private ServiceBookingDateWiseSummarySrv serviceBookingDateWiseSummarySrv;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Autowired
    private PoojaBookingRepository poojaBookingRepository;

    @Autowired
    private Util util;

    @Transactional
    public Map<String, String> bookPooja(JsonNode jsonNode, String category) throws Exception {



        Map<String, String> response = new HashMap<>();

        // Simple fields
        String preparedDt = jsonNode.path("preparedDt").asText();
        String preparedBy = jsonNode.path("preparedBy").asText();
        String itemCode = jsonNode.path("itemCode").asText();
        Integer noOfPerson = jsonNode.path("noOfPerson").asInt();
        String mobile = jsonNode.path("mobile").asText();
        String serviceDate = jsonNode.path("serviceDate").asText();
        Double rate = jsonNode.path("rate").asDouble();
        Double amount = jsonNode.path("amount").asDouble();
        Integer currentBooking = jsonNode.path("currentBooking").asInt();
        String isdCode = jsonNode.path("isdCode").asText();


        Integer perDayQuata = (Integer) poojaBookingRepository.getPoojaDetails(Long.valueOf(itemCode)).get("perDayQuota");

        Integer totalBooking = poojaBookingRepository.getTotalBooking(Integer.parseInt(itemCode), 1, serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

        System.out.println("Total booking on 10th April "+totalBooking);
        System.out.println("current booking on 10th April "+currentBooking);
        System.out.println("Per Day Quota "+perDayQuata);

        int available = perDayQuata - totalBooking;

        System.out.println("Total available seats on 10th April "+ available);

        if(available < currentBooking){

            response.put("currentStatus", "no seats available");
            return response;
        }


        Debug.printDebugBoundary();
        System.out.println(preparedDt);
        System.out.println(preparedBy);
        System.out.println(itemCode);
        System.out.println(noOfPerson);
        System.out.println(mobile);
        System.out.println(serviceDate);
        System.out.println(rate);
        System.out.println(amount);
        System.out.println(currentBooking);

        Debug.printDebugBoundary();


        //Setting serviceBookingDTO
        ServiceBooking serviceBooking = new ServiceBooking();

        serviceBooking.setPreparedDt(preparedDt);
        serviceBooking.setPreparedBy(preparedBy);
        serviceBooking.setItemCode(Long.valueOf(itemCode));
        serviceBooking.setRate(rate);
        serviceBooking.setNoOfPerson(noOfPerson);
        serviceBooking.setMobile(mobile);
        serviceBooking.setAmount(amount);
        serviceBooking.setIsdCode(isdCode);
        serviceBooking.setServiceDate(serviceDate);

        Map<String, String> resp = serviceBookingSrv.saveServiceBookingData(serviceBooking, category);


        // setting serviceBookingDetailDTO
        // serviceBookingDetails is an array
        JsonNode serviceBookingDetails = jsonNode.path("serviceBookingDetails");
        if (serviceBookingDetails.isArray()) {

            for (JsonNode detail : serviceBookingDetails) {
                String v_Sno = detail.path("v_Sno").asText();
                String name = detail.path("name").asText();
                String genderCode = detail.path("genderCode").asText();
                String address = detail.path("address").asText();
                String countryCode = detail.path("countryCode").asText();
                String stateCode = detail.path("stateCode").asText();
                String cityCode = detail.path("cityCode").asText();
                String isMainDevotee = detail.path("isMainDevotee").asText();


                ServiceBookingDetailId id = new ServiceBookingDetailId();
                id.setDocId(Long.valueOf(resp.get("DocId")));
                id.setV_Sno(Integer.valueOf(v_Sno));


                // Create new entity and set all fields
                ServiceBookingDetail serviceBookingDetail = new ServiceBookingDetail();
                serviceBookingDetail.setId(id);
                serviceBookingDetail.setName(name);
                serviceBookingDetail.setGenderCode(Integer.valueOf(genderCode));
                serviceBookingDetail.setAddress(address);
                serviceBookingDetail.setCountryCode(countryCode);
                serviceBookingDetail.setStateCode(Integer.valueOf(stateCode));
                serviceBookingDetail.setCityCode(Integer.valueOf(cityCode));
                serviceBookingDetail.setIsMainDevotee(Integer.valueOf(isMainDevotee));

                // Process the data as needed
                System.out.println(detail.toPrettyString());

                serviceBookingDetailSrv.saveServiceBookingDetails(serviceBookingDetail);
            }
        }

        // Create and populate the composite ID
        ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
        id.setItemCode(Long.valueOf(itemCode));
        id.setV_Date(serviceBookingSrv.convertUnixTimestampToDate(serviceDate));
        id.setSite_Code(Integer.parseInt(util.getSiteCode()));

        // Create new entity and populate fields
        ServiceBookingDateWiseSummary summary = new ServiceBookingDateWiseSummary();

        summary.setId(id);
        summary.setTotalBooking(totalBooking + currentBooking);

        serviceBookingDateWiseSummarySrv.saveDateWiseSummary(summary);
        Debug.printDebugBoundary();
        System.out.println(resp);
        Debug.printDebugBoundary();
        return resp;

    }

    public String cancelBooking(JsonNode jsonNode){

        //System.out.println("site code "+util.getSiteCode());

        String docId = jsonNode.path("docId").asText();
        String cancelledDt = jsonNode.path("cancelledDt").asText();
        String serviceDate = jsonNode.path("serviceDate").asText();
        String itemCode = jsonNode.path("itemCode").asText();
        Integer currentBooking = jsonNode.path("currentBooking").asInt();

        poojaBookingRepository.cancelBooking(Long.valueOf(docId), serviceBookingSrv.convertUnixTimestampToFormattedDate(cancelledDt));

        ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
        id.setItemCode(Long.valueOf(itemCode));
        id.setV_Date(serviceBookingSrv.convertUnixTimestampToDate(serviceDate));
        id.setSite_Code(Integer.valueOf(util.getSiteCode()));


        Integer totalBooking = poojaBookingRepository.getTotalBooking(Integer.parseInt(itemCode), Integer.parseInt(util.getSiteCode()), serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

        if(totalBooking == 0){
            return "something went wrong😜";
        }

        if((totalBooking-currentBooking) < 0){
            return "something went wrong😜";
        }


        // Create new entity and populate fields
        ServiceBookingDateWiseSummary summary = new ServiceBookingDateWiseSummary();

        summary.setId(id);
        summary.setTotalBooking(totalBooking - currentBooking);

        serviceBookingDateWiseSummarySrv.saveDateWiseSummary(summary);
        paymentDetailRepository.deleteByDocId(Long.parseLong(docId));


        return "Your booking has been cancelled successfully";

    }

}
