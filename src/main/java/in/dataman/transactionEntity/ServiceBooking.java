package in.dataman.transactionEntity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ServiceBooking")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceBooking {

    @Id
    private Long docId;

    private String u_Name;
    private Integer v_Type;
    private Integer v_No;
    private String recIdPrefix;
    private String recId;
    private Integer v_Prefix;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String v_Date;

    private Double v_Time;
    private Integer site_Code;
    private String preparedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String preparedDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String u_EntDt_LatestLine;

    private Integer noOfPerson;

    private Long itemCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String serviceDate;

    private Double rate;
    private Double amount;

    private Long paymentId;  //bigint	Checked
    private String cancelledBy;	//varchar(10)	Checked

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String cancelledDt;  //datetime	Checked

    private Integer noOfBooking;
    private String serviceNature;


}
