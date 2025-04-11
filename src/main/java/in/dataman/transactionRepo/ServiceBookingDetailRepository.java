package in.dataman.transactionRepo;

import in.dataman.transactionEntity.ServiceBookingDetail;
import in.dataman.transactionEntity.ServiceBookingDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceBookingDetailRepository extends JpaRepository<ServiceBookingDetail, ServiceBookingDetailId> {

}
