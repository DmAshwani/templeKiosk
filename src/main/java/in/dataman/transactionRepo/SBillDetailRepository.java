package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionDTO.CompositeKey;
import in.dataman.transactionEntity.SBillDetailEntity;

@Repository
public interface SBillDetailRepository extends JpaRepository<SBillDetailEntity, CompositeKey> {

}
