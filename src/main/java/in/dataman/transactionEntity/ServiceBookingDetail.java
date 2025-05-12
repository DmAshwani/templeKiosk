package in.dataman.transactionEntity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "serviceBookingDetail")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceBookingDetail {


    @EmbeddedId
    private ServiceBookingDetailId id;

    private String name;
    private Integer genderCode;
    private String address;
    private String countryCode;
    private Integer stateCode;
    private Integer cityCode;
    private Integer isMainDevotee;
    private String mobile;
    private String isdCode;

}
