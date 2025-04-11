package in.dataman.transactionRepo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.PaymentDetail;
import jakarta.transaction.Transactional;



@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

	Optional<PaymentDetail> findByResTransRefId(String resTransRefId);

	Optional<PaymentDetail> findByDocId(Long docId);
	
	@Modifying
    @Transactional
	void deleteByDocId(Long docId);
}
