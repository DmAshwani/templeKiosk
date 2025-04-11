package in.dataman.transactionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueDTO {
	private String isdCode;
	private String mobile;
	private String noOfperson;
	private String preparedDt;
}
