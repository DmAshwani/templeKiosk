package in.dataman.transactionService;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.debug.Debug;
import in.dataman.exceptions.BookingException;
import in.dataman.transactionEntity.*;
import in.dataman.transactionRepo.PaymentDetailRepository;
import in.dataman.transactionRepo.PoojaBookingRepository;
import in.dataman.transactionRepo.ServiceBookingDetailRepository;
import in.dataman.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    private PoojaBookingService poojaBookingService;

    @Autowired
    private ServiceBookingDetailRepository serviceBookingDetailRepository;


    @Transactional
    public Map<String, String> bookPooja(JsonNode jsonNode, String category) throws Exception {



        try{
            Map<String, String> response = new HashMap<>();

            // Simple fields
            String preparedDt = jsonNode.path("preparedDt").asText();
            String preparedBy = jsonNode.path("preparedBy").asText();
            String itemCode = jsonNode.path("itemCode").asText();
            Integer noOfPerson = jsonNode.path("noOfPerson").asInt();

            String serviceDate = jsonNode.path("serviceDate").asText();
            Double rate = jsonNode.path("rate").asDouble();
            Double amount = jsonNode.path("amount").asDouble();
            Integer currentBooking = jsonNode.path("currentBooking").asInt();



            //Integer perDayQuata = (Integer) poojaBookingRepository.getPoojaDetails(Long.valueOf(itemCode)).get("perDayQuota");

            Integer totalBooking = poojaBookingRepository.getTotalBooking(Long.parseLong(itemCode), 1, serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

            System.out.println("Total booking on 10th April "+totalBooking);
            System.out.println("current booking on 10th April "+currentBooking);
//        System.out.println("Per Day Quota "+perDayQuata);

            //int available = perDayQuata - totalBooking;

//        System.out.println("Total available seats on 10th April "+ available);
//
//        if(available < currentBooking){
//
//            response.put("currentStatus", "no seats available");
//            return response;
//        }


            Debug.printDebugBoundary();
            System.out.println(preparedDt);
            System.out.println(preparedBy);
            System.out.println(itemCode);
            System.out.println(noOfPerson);

            System.out.println(serviceDate);
            System.out.println(rate);
            System.out.println(amount);
            System.out.println(currentBooking);

            Debug.printDebugBoundary();

            //count and insert with Zero booking
            int statusCount = poojaBookingRepository.getBookingSummaryCount(Integer.parseInt(util.getSiteCode()), Long.parseLong(itemCode), serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

            if(statusCount == 0){

                insertServiceBookingDateWiseSummaryData(util.getSiteCode(), Long.valueOf(itemCode), serviceDate);
                // Continue with rest of your logic here
                System.out.println("Continuing with the rest of the program...");


            }
            //Setting serviceBookingDTO
            ServiceBooking serviceBooking = new ServiceBooking();

            serviceBooking.setPreparedDt(preparedDt);
            serviceBooking.setPreparedBy(preparedBy);
            serviceBooking.setItemCode(Long.valueOf(itemCode));
            serviceBooking.setRate(rate);
            serviceBooking.setNoOfPerson(noOfPerson);
            serviceBooking.setAmount(amount);
            serviceBooking.setServiceDate(serviceDate);
            serviceBooking.setNoOfBooking(currentBooking);
            serviceBooking.setServiceNature((String) poojaBookingRepository.getPoojaDetails(Long.valueOf(itemCode)).get("serviceType"));

            System.out.println(serviceBooking);

            Map<String, String> resp = serviceBookingSrv.saveServiceBookingData(serviceBooking, category);

            System.out.println("RESPONSE "+ resp);

            // setting serviceBookingDetailDTO
            // serviceBookingDetails is an array
            JsonNode serviceBookingDetails = jsonNode.path("serviceBookingDetails");
            if (serviceBookingDetails.isArray()) {

                int i = 0;
                for (JsonNode detail : serviceBookingDetails) {
                    //check key value


                    i = i+1;
                    String v_Sno = detail.path("v_Sno").asText();
                    String name = detail.path("name").asText();
                    String genderCode = detail.path("genderCode").asText();
                    String address = detail.path("address").asText();
                    String countryCode = detail.path("countryCode").asText();
                    String stateCode = detail.path("stateCode").asText();
                    String cityCode = detail.path("cityCode").asText();
                    String isMainDevotee = detail.path("isMainDevotee").asText();
                    String mobile = detail.path("mobile").asText();
                    String isdCode = detail.path("isdCode").asText();

                    System.out.println("Mobile No "+mobile);
                    System.out.println("Isd "+isdCode);

                    if(!Objects.equals(name.trim(), "")){


                        ServiceBookingDetailId id = new ServiceBookingDetailId();
                        id.setDocId(Long.valueOf(resp.get("DocId")));
                        //id.setV_Sno(Integer.valueOf(v_Sno));
                        id.setV_Sno(i);

                        serviceBookingDetailRepository.deleteById(id);

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
                        serviceBookingDetail.setMobile(mobile);
                        serviceBookingDetail.setIsdCode(isdCode);

                        // Process the data as needed
                        System.out.println(detail.toPrettyString());

                        serviceBookingDetailSrv.saveServiceBookingDetails(serviceBookingDetail);
                    }


                }
            }
            //managestatus.
            System.out.println("RESPONSE "+ resp.get("DocId"));
            poojaBookingService.manageServiceBookingStaus(resp.get("DocId"), false);

            return resp;
        }catch (BookingException be){
            return Map.of("status", be.getMessage());
        }
        catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof java.sql.SQLException sqlException) {
                if (sqlException.getErrorCode() == 2627) {
                    // Primary key or unique constraint violation
                    return Map.of("status", "refresh and try again");
                }
            }
            return Map.of("status", "some other violation of data integrity");
        }





    }

    private  void insertServiceBookingDateWiseSummaryData(String siteCode, Long itemCode, String serviceDate) throws Exception{

        try{

            int statusCount = poojaBookingRepository.getBookingSummaryCount(Integer.parseInt(util.getSiteCode()), Long.parseLong(String.valueOf(itemCode)), serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

            ServiceBookingDateWiseSummary summary = null;
            if(statusCount == 0){

                ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
                id.setItemCode(Long.valueOf(itemCode));
                id.setServiceDate(serviceBookingSrv.convertUnixTimestampToDate(serviceDate));
                id.setSite_Code(Integer.parseInt(siteCode));

                summary = new ServiceBookingDateWiseSummary();
                summary.setId(id);
                summary.setTotalBooking(0);
                summary.setIsStatus(0);

            }




            serviceBookingDateWiseSummarySrv.saveDateWiseSummary(summary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String cancelBooking(JsonNode jsonNode){

        //System.out.println("site code "+util.getSiteCode());

        //manage status; with true isDelete

        String docId = jsonNode.path("docId").asText();
        String cancelledDt = jsonNode.path("cancelledDt").asText();
        String serviceDate = jsonNode.path("serviceDate").asText();


        poojaBookingService.manageServiceBookingStaus(docId, true);

        poojaBookingRepository.cancelBooking(Long.valueOf(docId), serviceBookingSrv.convertUnixTimestampToFormattedDate(cancelledDt));

        //poojaBookingService.manageServiceBookingStaus(docId, true);
        return "Your booking has been cancelled successfully";

    }
}
