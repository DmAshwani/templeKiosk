package in.dataman.transactionEntity;


import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "serviceBookingDateWiseSummary")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceBookingDateWiseSummary {

    @EmbeddedId
    private ServiceBookingDateWiseSummaryId id;

    private Integer totalBooking;
}






