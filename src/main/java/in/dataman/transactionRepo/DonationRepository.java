package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.Donation;



@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
	
}
