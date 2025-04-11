package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.SafeDepositEntity;

@Repository
public interface SafeDepositRepository extends JpaRepository<SafeDepositEntity, Long>{

}
