package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.SBillEntity;

@Repository
public interface SBillRepository extends JpaRepository<SBillEntity, Long> {

}
