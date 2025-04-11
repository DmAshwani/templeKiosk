package in.dataman.transactionDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SafeDepositDTO {
    private String mobile;
    private String noOfItems;
    private String itemDescription;
    private String preparedDt;
  }
