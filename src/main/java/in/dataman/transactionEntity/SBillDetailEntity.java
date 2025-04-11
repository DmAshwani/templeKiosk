package in.dataman.transactionEntity;

import java.io.Serializable;

import in.dataman.transactionDTO.CompositeKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@SuppressWarnings("serial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "sBillDetail")
public class SBillDetailEntity implements Serializable {

 

    @EmbeddedId
    private CompositeKey id;

    private String itemCode;
    private String unitCode;
    private String rate;
    private String skuRate;
    private String quantity;
    private String skuQty;
    private String oh_Amt_Gross;
    private String oh_Amt_Taxable;
    private String oh_Amt_Net;
}

