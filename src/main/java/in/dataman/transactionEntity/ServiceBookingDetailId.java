package in.dataman.transactionEntity;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
@Embeddable
public class ServiceBookingDetailId implements Serializable {

    private Long docId;
    private Integer v_Sno;

    public ServiceBookingDetailId() {}

    public ServiceBookingDetailId(Long docId, Integer v_Sno) {
        this.docId = docId;
        this.v_Sno = v_Sno;
    }

    // Getters and setters

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Integer getV_Sno() {
        return v_Sno;
    }

    public void setV_Sno(Integer v_Sno) {
        this.v_Sno = v_Sno;
    }

    // equals and hashCode are required

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceBookingDetailId)) return false;
        ServiceBookingDetailId that = (ServiceBookingDetailId) o;
        return Objects.equals(docId, that.docId) &&
                Objects.equals(v_Sno, that.v_Sno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, v_Sno);
    }
}

