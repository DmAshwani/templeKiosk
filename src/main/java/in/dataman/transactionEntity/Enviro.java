package in.dataman.transactionEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name="enviro")
public class Enviro {
	
	@Id
	private Integer site_Code;
	
	private Integer allowedPersonPerQueue;
	
	private String devoteeControlAc;
}
