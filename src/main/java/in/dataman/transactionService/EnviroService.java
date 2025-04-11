package in.dataman.transactionService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.dataman.transactionEntity.Enviro;
import in.dataman.transactionRepo.EnviroRepository;

@Service
public class EnviroService {

	@Autowired
	private EnviroRepository enviroRepository;

	public Optional<Enviro> getEnviroById(Integer id) {
		return enviroRepository.findById(id);
	}

}
