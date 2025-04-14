package in.dataman.transactionEntity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Setter
@Getter
@Embeddable
public class ServiceBookingDateWiseSummaryId implements Serializable {

    private Long itemCode;
    private String serviceDate;
    private Integer site_Code;

    public ServiceBookingDateWiseSummaryId() {}

    public ServiceBookingDateWiseSummaryId(Long itemCode, String v_Date, Integer site_Code) {
        this.itemCode = itemCode;
        this.serviceDate = v_Date;
        this.site_Code = site_Code;
    }

    // Getters and setters

    // equals and hashCode (required for composite key)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceBookingDateWiseSummaryId)) return false;
        ServiceBookingDateWiseSummaryId that = (ServiceBookingDateWiseSummaryId) o;
        return Objects.equals(itemCode, that.itemCode) &&
                Objects.equals(serviceDate, that.serviceDate) &&
                Objects.equals(site_Code, that.site_Code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode, serviceDate, site_Code);
    }
}
