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
@Table(name="safeDeposit")
public class SafeDepositEntity {
	
	@Id
	private Long docId;
	
	private Integer v_Type;
	private Integer v_No;
	private String recIdPrefix;
	private String recId;
	private Integer v_Prefix;
	private String v_Date;
	private Double v_Time;
	private Integer site_Code;
	private String preparedDt;
	private String preparedBy;
	private String isdCode;
	private String mobile;
	private String noOfPcs;
	private String itemDescription;
}
