package in.dataman.transactionRepo;

import in.dataman.transactionEntity.ServiceBookingDateWiseSummary;
import in.dataman.transactionEntity.ServiceBookingDateWiseSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceBookingDateWiseSummaryRepository extends JpaRepository<ServiceBookingDateWiseSummary, ServiceBookingDateWiseSummaryId> {

}
