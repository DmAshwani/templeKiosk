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
@Table(name="pujaTrans")
public class PujaTrans {
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
	private String preparedByIP;
	private String u_Name;
	private String u_EntDt;
	private String u_EntDt_LatestLine;
	private String approvedBy;
	private String approvalDateTime;
	private String wtaCode;
	private String isdCode;
	private String mobile;
	private String noOfPerson;
	private String itemCode;
	private String slotDate;
	private String rate;
	private String amount;
	private String paymentId;
}
