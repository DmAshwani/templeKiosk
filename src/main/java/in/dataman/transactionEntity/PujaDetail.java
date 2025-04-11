package in.dataman.transactionEntity;

import in.dataman.transactionDTO.CompositeKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "pujaDetail")
public class PujaDetail {

	@EmbeddedId
    private CompositeKey id;
	
	private String name;
	private String genderCode;
	
}
