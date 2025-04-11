package in.dataman.transactionDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrasadBookingDTO {
    
    private String name;
    private String mobile;
    private String partyCode;
    private String total;
    private String preparedDt;
    private List<Prasad> prasad;
}
