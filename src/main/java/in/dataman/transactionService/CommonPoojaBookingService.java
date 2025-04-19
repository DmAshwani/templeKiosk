package in.dataman.transactionService;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.debug.Debug;
import in.dataman.exceptions.BookingException;
import in.dataman.transactionEntity.*;
import in.dataman.transactionRepo.PaymentDetailRepository;
import in.dataman.transactionRepo.PoojaBookingRepository;
import in.dataman.transactionRepo.ServiceBookingDetailRepository;
import in.dataman.transactionRepo.ServiceBookingRepository;
import in.dataman.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.DefaultTransactionDefinition;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    private PoojaBookingService poojaBookingService;

    @Autowired
    private ServiceBookingDetailRepository serviceBookingDetailRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ServiceBookingRepository serviceBookingRepository;



    public Map<String, String> bookPooja(JsonNode jsonNode, String category) throws Exception {

        TransactionStatus transStatus = null;

        try {
            transStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

            Map<String, String> response = new HashMap<>();

            // Your existing code (same as before)
            String preparedDt = jsonNode.path("preparedDt").asText();
            String preparedBy = jsonNode.path("preparedBy").asText();
            String itemCode = jsonNode.path("itemCode").asText();
            Integer noOfPerson = jsonNode.path("noOfPerson").asInt();

            String serviceDate = jsonNode.path("serviceDate").asText();
            Double rate = jsonNode.path("rate").asDouble();
            Double amount = jsonNode.path("amount").asDouble();
            Integer currentBooking = jsonNode.path("currentBooking").asInt();

            Integer totalBooking = poojaBookingRepository.getTotalBooking(Long.parseLong(itemCode), 1, serviceBookingSrv.convertUnixTimestampToDate(serviceDate));

            int statusCount = poojaBookingRepository.getBookingSummaryCount(
                    Integer.parseInt(util.getSiteCode()),
                    Long.parseLong(itemCode),
                    serviceBookingSrv.convertUnixTimestampToDate(serviceDate)
            );

            if (statusCount == 0) {
                insertServiceBookingDateWiseSummaryData(util.getSiteCode(), Long.valueOf(itemCode), serviceDate);
            }

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

            Map<String, String> resp = serviceBookingSrv.saveServiceBookingData(serviceBooking, category);
            serviceBookingRepository.flush();

            JsonNode serviceBookingDetails = jsonNode.path("serviceBookingDetails");
            if (serviceBookingDetails.isArray()) {
                int i = 0;
                for (JsonNode detail : serviceBookingDetails) {
                    i++;
                    ServiceBookingDetailId id = new ServiceBookingDetailId();
                    id.setDocId(Long.valueOf(resp.get("DocId")));
                    id.setV_Sno(i);
                    serviceBookingDetailRepository.deleteById(id);

                    ServiceBookingDetail serviceBookingDetail = new ServiceBookingDetail();

                    serviceBookingDetail.setName(
                            detail.hasNonNull("name") && !detail.get("name").asText().isBlank()
                                    ? detail.get("name").asText()
                                    : null
                    );

                    serviceBookingDetail.setId(id);

                    serviceBookingDetail.setGenderCode(
                            detail.hasNonNull("genderCode") && !detail.get("genderCode").asText().isBlank()
                                    ? Integer.parseInt(detail.get("genderCode").asText())
                                    : null
                    );

                    serviceBookingDetail.setAddress(
                            detail.hasNonNull("address") && !detail.get("address").asText().isBlank()
                                    ? detail.get("address").asText()
                                    : null
                    );

                    serviceBookingDetail.setCountryCode(
                            detail.hasNonNull("countryCode") && !detail.get("countryCode").asText().isBlank()
                                    ? detail.get("countryCode").asText()
                                    : null
                    );

                    serviceBookingDetail.setStateCode(
                            detail.hasNonNull("stateCode") && !detail.get("stateCode").asText().isBlank()
                                    ? Integer.parseInt(detail.get("stateCode").asText())
                                    : null
                    );

                    serviceBookingDetail.setCityCode(
                            detail.hasNonNull("cityCode") && !detail.get("cityCode").asText().isBlank()
                                    ? detail.get("cityCode").asInt()
                                    : null
                    );

                    serviceBookingDetail.setIsMainDevotee(
                            detail.hasNonNull("isMainDevotee") && !detail.get("isMainDevotee").asText().isBlank()
                                    ? Integer.parseInt(detail.get("isMainDevotee").asText())
                                    : null
                    );

                    serviceBookingDetail.setMobile(
                            detail.hasNonNull("mobile") && !detail.get("mobile").asText().isBlank()
                                    ? detail.get("mobile").asText()
                                    : null
                    );

                    serviceBookingDetail.setIsdCode(
                            detail.hasNonNull("isdCode") && !detail.get("isdCode").asText().isBlank()
                                    ? detail.get("isdCode").asText()
                                    : null
                    );


                    serviceBookingDetailSrv.saveServiceBookingDetails(serviceBookingDetail);
                }
            }


            serviceBookingDetailRepository.flush();
            poojaBookingService.manageServiceBookingStaus(resp.get("DocId"), false);

            transactionManager.commit(transStatus);
            transStatus= null;
            return resp;

        } catch (BookingException be) {
            if(transStatus != null){
                transactionManager.rollback(transStatus);
            }

            return Map.of("status", be.getMessage());

        } catch (Exception e) {
            if(transStatus != null){
                transactionManager.rollback(transStatus);
            }
            throw e; // rethrow other exceptions
        }
    }

    private  void insertServiceBookingDateWiseSummaryData(String siteCode, Long itemCode, String serviceDate) throws Exception{

        try{

            ServiceBookingDateWiseSummary summary = null;
            ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
            id.setItemCode(Long.valueOf(itemCode));
            id.setServiceDate(serviceBookingSrv.convertUnixTimestampToDate(serviceDate));
            id.setSite_Code(Integer.parseInt(siteCode));

            summary = new ServiceBookingDateWiseSummary();
            summary.setId(id);
            summary.setTotalBooking(0);
            summary.setIsStatus(0);

            serviceBookingDateWiseSummarySrv.saveDateWiseSummary(summary);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof java.sql.SQLException sqlException) {
                if (sqlException.getErrorCode() != 2627) {
                    throw e;
                }
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String cancelBooking(JsonNode jsonNode){

        String docId = jsonNode.path("docId").asText();
        String cancelledDt = jsonNode.path("cancelledDt").asText();
        String serviceDate = jsonNode.path("serviceDate").asText();

        poojaBookingService.manageServiceBookingStaus(docId, true);

        poojaBookingRepository.cancelBooking(Long.valueOf(docId), serviceBookingSrv.convertUnixTimestampToFormattedDate(cancelledDt));

        //poojaBookingService.manageServiceBookingStaus(docId, true);
        return "Your booking has been cancelled successfully";

    }
}
