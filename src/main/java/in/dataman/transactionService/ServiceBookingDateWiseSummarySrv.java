package in.dataman.transactionService;

import org.springframework.stereotype.Service;

import in.dataman.transactionEntity.ServiceBookingDateWiseSummary;
import in.dataman.transactionEntity.ServiceBookingDateWiseSummaryId;
import in.dataman.transactionRepo.ServiceBookingDateWiseSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceBookingDateWiseSummarySrv {

    @Autowired
    private ServiceBookingDateWiseSummaryRepository repository;

    public ServiceBookingDateWiseSummary saveDateWiseSummary(ServiceBookingDateWiseSummary dto) {
        // Create and populate the composite ID
        ServiceBookingDateWiseSummaryId id = new ServiceBookingDateWiseSummaryId();
        id.setItemCode(dto.getId().getItemCode());
        id.setV_Date(dto.getId().getV_Date());
        id.setSite_Code(dto.getId().getSite_Code());

        // Create new entity and populate fields
        ServiceBookingDateWiseSummary summary = new ServiceBookingDateWiseSummary();

        summary.setId(id);
        summary.setTotalBooking(dto.getTotalBooking());



        return repository.save(summary);
    }
}
