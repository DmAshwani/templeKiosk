package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.QueueEntity;

@Repository
public interface QueueRepository extends JpaRepository<QueueEntity, Long> {

}
