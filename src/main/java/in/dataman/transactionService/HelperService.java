package in.dataman.transactionService;

import in.dataman.Enums.PaymentMode;
import in.dataman.Enums.PaymentStatus;
import in.dataman.transactionEntity.PaymentDetail;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HelperService {


//    if(serviceBookingDto.getAmount() != 0){
//
//        String orderId = UUID.randomUUID().toString().replace("-", "");
//        String razorpayOrderId = razorpayService.createOrder(Double.parseDouble(amount), "INR", orderId);
//
//        Map<String, String> qrCodeObj = generateUPIQRCode(razorpayOrderId, Double.parseDouble(amount));
//
//        PaymentDetail paymentDetail = new PaymentDetail();
//        paymentDetail.setV_Type(Integer.parseInt(voucherType));
//        paymentDetail.setRecId(recId.getRecIdValue());
//        paymentDetail.setV_Date(convertUnixTimestampToDate(serviceBookingDto.getPreparedDt()));
//        paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(serviceBookingDto.getPreparedDt())));
//        paymentDetail.setDocId(docId);
//        paymentDetail.setSite_Code(Short.valueOf(util.getSiteCode()));
//        paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
//        paymentDetail.setOnlineTransId(orderId);
//        paymentDetail.setResTransRefId(razorpayOrderId);
//        paymentDetail.setAmount(Double.parseDouble(amount));
//        paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
//        paymentDetail.setPreparedDt(convertUnixTimestampToFormattedDate(serviceBookingDto.getPreparedDt()));
//        paymentDetailRepository.save(paymentDetail);
//
//
//        Optional<PaymentDetail> pdoptional = paymentDetailRepository.findByResTransRefId(razorpayOrderId);
//
//        pdoptional.ifPresent(pd -> {
//            serviceBooking.setPaymentId(pd.getId());
//            serviceBookingRepository.save(serviceBooking);
//        });
//
//
//
//        response.put("qrCode", qrCodeObj.get("image_url"));
//        response.put("orderId", razorpayOrderId);
//        response.put("upiUrl", qrCodeObj.get("qr_id"));
//
//
//    }
}
