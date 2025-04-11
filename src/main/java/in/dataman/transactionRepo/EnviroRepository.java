package in.dataman.transactionRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.dataman.transactionEntity.Enviro;

@Repository
public interface EnviroRepository extends JpaRepository<Enviro, Integer> {
}