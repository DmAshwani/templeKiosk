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
@Table(name="donation")
public class Donation {
	
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
	private String amount;
	private Integer paymentMode;
	private Integer status;
	private String name;
	private String add1;
	private String add2;
	private String countryCode;
	private String stateCode;
	private String cityCode;
	private String mobile;
	private String pin;
	private String isdCode;
	private String pan;
	private Long paymentId;
	private String cancelledBy;
	private String cancelledDt;
	
	
}

