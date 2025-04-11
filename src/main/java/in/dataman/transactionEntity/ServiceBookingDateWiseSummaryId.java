package in.dataman.transactionEntity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class ServiceBookingDateWiseSummaryId implements Serializable {

    private Long itemCode;
    private String v_Date;
    private Integer site_Code;

    public ServiceBookingDateWiseSummaryId() {}

    public ServiceBookingDateWiseSummaryId(Long itemCode, String v_Date, Integer site_Code) {
        this.itemCode = itemCode;
        this.v_Date = v_Date;
        this.site_Code = site_Code;
    }

    // Getters and setters

    public Long getItemCode() {
        return itemCode;
    }

    public void setItemCode(Long itemCode) {
        this.itemCode = itemCode;
    }

    public String getV_Date() {
        return v_Date;
    }

    public void setV_Date(String v_Date) {
        this.v_Date = v_Date;
    }

    public Integer getSite_Code() {
        return site_Code;
    }

    public void setSite_Code(Integer site_Code) {
        this.site_Code = site_Code;
    }

    // equals and hashCode (required for composite key)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceBookingDateWiseSummaryId)) return false;
        ServiceBookingDateWiseSummaryId that = (ServiceBookingDateWiseSummaryId) o;
        return Objects.equals(itemCode, that.itemCode) &&
                Objects.equals(v_Date, that.v_Date) &&
                Objects.equals(site_Code, that.site_Code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCode, v_Date, site_Code);
    }
}
