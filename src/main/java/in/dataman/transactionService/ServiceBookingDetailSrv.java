package in.dataman.transactionService;

import in.dataman.transactionEntity.ServiceBookingDetail;
import in.dataman.transactionEntity.ServiceBookingDetailId;
import in.dataman.transactionRepo.ServiceBookingDetailRepository;
import in.dataman.transactionRepo.ServiceBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceBookingDetailSrv {

    @Autowired
    private ServiceBookingDetailRepository serviceBookingDetailRepository;

    public ServiceBookingDetail saveServiceBookingDetails(ServiceBookingDetail serviceBookingDetailDto){


        // Create and populate the composite key
        ServiceBookingDetailId id = new ServiceBookingDetailId();
        id.setDocId(serviceBookingDetailDto.getId().getDocId());
        id.setV_Sno(serviceBookingDetailDto.getId().getV_Sno());

        // Create new entity and set all fields
        ServiceBookingDetail serviceBookingDetail = new ServiceBookingDetail();
        serviceBookingDetail.setId(id);
        serviceBookingDetail.setName(serviceBookingDetailDto.getName());
        serviceBookingDetail.setGenderCode(serviceBookingDetailDto.getGenderCode());
        serviceBookingDetail.setAddress(serviceBookingDetailDto.getAddress());
        serviceBookingDetail.setCountryCode(serviceBookingDetailDto.getCountryCode());
        serviceBookingDetail.setStateCode(serviceBookingDetailDto.getStateCode());
        serviceBookingDetail.setCityCode(serviceBookingDetailDto.getCityCode());
        serviceBookingDetail.setIsMainDevotee(serviceBookingDetailDto.getIsMainDevotee());

        return serviceBookingDetailRepository.save(serviceBookingDetail);

    }

}
