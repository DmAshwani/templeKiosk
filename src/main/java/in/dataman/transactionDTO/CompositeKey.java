package in.dataman.transactionDTO;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.*;

@SuppressWarnings("serial")
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompositeKey implements Serializable {
    private Long docId;
    private String v_SNo;
}

