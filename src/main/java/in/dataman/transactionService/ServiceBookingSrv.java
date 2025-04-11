package in.dataman.transactionService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.razorpay.QrCode;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import dataman.dmbase.dto.RecId;
import dataman.dmbase.server.DmBaseServer;
import in.dataman.Enums.PaymentMode;
import in.dataman.Enums.PaymentStatus;
import in.dataman.Enums.VoucherCategory;
import in.dataman.transactionEntity.PaymentDetail;
import in.dataman.transactionEntity.ServiceBooking;
import in.dataman.transactionRepo.PaymentDetailRepository;
import in.dataman.transactionRepo.ServiceBookingRepository;
import in.dataman.util.Util;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class ServiceBookingSrv {

    @Autowired
    private ServiceBookingRepository serviceBookingRepository;

    @Autowired
    private DmBaseServer dmBaseServer;

    @Autowired
    @Qualifier("TransactionJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentDetailRepository paymentDetailRepository;

    @Autowired
    private Util util;

    public Map<String, String> saveServiceBookingData(ServiceBooking serviceBookingDto, String category) throws Exception {

        Long docId = null;
        RecId recId = null;

        String voucherType = null;
        String vPrefix = null;

        String poojaBooking = String.valueOf(VoucherCategory.PUJA_BOOKING);

        if(category.equals(poojaBooking)){

            System.out.println("Category Pooja Booking");

            voucherType = fetchVoucherType(VoucherCategory.PUJA_BOOKING.getShortName());
            vPrefix = fetchVoucherPrefix(String.valueOf(serviceBookingDto.getPreparedDt()));
            docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, util.getSiteCode(), jdbcTemplate));

            recId = dmBaseServer.getRecId("donation", "docId", docId.toString(), "recId", new RecId(),
                    convertUnixTimestampToDate(String.valueOf(serviceBookingDto.getPreparedDt())), "v_Type", voucherType, vPrefix, util.getSiteCode(), "HO",
                    "1", true, null, jdbcTemplate);

        }else{
            System.out.println("Category trustee Pooja Booking");

            voucherType = fetchVoucherType(VoucherCategory.TRUSTEE_PUJA_BOOKING.getShortName());
            vPrefix = fetchVoucherPrefix(String.valueOf(serviceBookingDto.getPreparedDt()));
            docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, util.getSiteCode(), jdbcTemplate));

            recId = dmBaseServer.getRecId("donation", "docId", docId.toString(), "recId", new RecId(),
                    convertUnixTimestampToDate(String.valueOf(serviceBookingDto.getPreparedDt())), "v_Type", voucherType, vPrefix, util.getSiteCode(), "HO",
                    "1", true, null, jdbcTemplate);

        }



        ServiceBooking serviceBooking = new ServiceBooking();

        serviceBooking.setDocId(docId);
        serviceBooking.setV_Type(Integer.parseInt(voucherType));
        serviceBooking.setV_No(recId.getCounter().intValue());
        serviceBooking.setV_Date(convertUnixTimestampToDate(serviceBookingDto.getPreparedDt()));
        serviceBooking.setRecIdPrefix(recId.getPrefix());
        serviceBooking.setRecId(recId.getRecIdValue());
        serviceBooking.setV_Prefix(Integer.parseInt(vPrefix));
        serviceBooking.setV_Time(Double.parseDouble(convertUnixTimestampToTime(serviceBookingDto.getPreparedDt())));
        serviceBooking.setSite_Code(Integer.parseInt(util.getSiteCode()));


        serviceBooking.setPreparedBy("kiosk");
        serviceBooking.setPreparedDt(convertUnixTimestampToFormattedDate(serviceBookingDto.getPreparedDt()));
        serviceBooking.setU_EntDt_LatestLine(convertUnixTimestampToFormattedDate(serviceBookingDto.getPreparedDt()));
        serviceBooking.setItemCode(serviceBookingDto.getItemCode());
        serviceBooking.setNoOfPerson(serviceBookingDto.getNoOfPerson());
        serviceBooking.setMobile(serviceBookingDto.getMobile());
        serviceBooking.setIsdCode(serviceBookingDto.getIsdCode());
        serviceBooking.setServiceDate(convertUnixTimestampToFormattedDate(serviceBookingDto.getServiceDate()));
        serviceBooking.setRate(serviceBookingDto.getRate());
        serviceBooking.setAmount(serviceBookingDto.getAmount());

        String amount = String.valueOf(serviceBookingDto.getAmount());


        serviceBookingRepository.save(serviceBooking);

        if(serviceBookingDto.getAmount() == 0){
            return Map.of("docId", docId.toString());
        }

        String orderId = UUID.randomUUID().toString().replace("-", "");
        String razorpayOrderId = razorpayService.createOrder(Double.parseDouble(amount), "INR", orderId);

        Map<String, String> qrCodeObj = generateUPIQRCode(razorpayOrderId, Double.parseDouble(amount));

        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setV_Type(Integer.parseInt(voucherType));
        paymentDetail.setRecId(recId.getRecIdValue());
        paymentDetail.setV_Date(convertUnixTimestampToDate(serviceBookingDto.getPreparedDt()));
        paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(serviceBookingDto.getPreparedDt())));
        paymentDetail.setDocId(docId);
        paymentDetail.setSite_Code(Short.valueOf(util.getSiteCode()));
        paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
        paymentDetail.setOnlineTransId(orderId);
        paymentDetail.setResTransRefId(razorpayOrderId);
        paymentDetail.setAmount(Double.parseDouble(amount));
        paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
        paymentDetail.setPreparedDt(convertUnixTimestampToFormattedDate(serviceBookingDto.getPreparedDt()));
        paymentDetailRepository.save(paymentDetail);

        Optional<PaymentDetail> pdoptional = paymentDetailRepository.findByResTransRefId(razorpayOrderId);

        pdoptional.ifPresent(pd -> {
            serviceBooking.setPaymentId(pd.getId());
            serviceBookingRepository.save(serviceBooking);
        });


        Map<String, String> response = new HashMap<>();
        response.put("qrCode", qrCodeObj.get("image_url"));
        response.put("orderId", razorpayOrderId);
        response.put("upiUrl", qrCodeObj.get("qr_id"));
        response.put("DocId", docId.toString());

        return response;

    }

    private String fetchVoucherType(String vtCategory) {
        String sql = "SELECT v_Type FROM voucher_Type WHERE isActive = 1 AND category = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, vtCategory))
                .orElseThrow(() -> new RuntimeException("Voucher Type not found"));
    }

    private String fetchVoucherPrefix(String unixTimestamp) {
        long timestamp = Long.parseLong(unixTimestamp);
        LocalDate date = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        int year = date.getYear();
        int month = date.getMonthValue();
        return String.valueOf(month >= 4 ? year : year - 1).substring(2);
    }

    public String convertUnixTimestampToDate(String unixTimestamp) {
        // Convert the string Unix timestamp to a long
        long timestamp = Long.parseLong(unixTimestamp);

        // Convert Unix timestamp to Instant
        Instant instant = Instant.ofEpochSecond(timestamp);

        // Convert Instant to ZonedDateTime with system default timezone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        // Format the date in "dd/MM/yyyy" format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return zonedDateTime.format(formatter);
    }

    public String convertUnixTimestampToTime(String unixTimestamp) {
        // Convert the string Unix timestamp to a long
        long timestamp = Long.parseLong(unixTimestamp);

        // Convert Unix timestamp to Instant
        Instant instant = Instant.ofEpochSecond(timestamp);

        // Convert Instant to ZonedDateTime with system default timezone
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        // Get the hours and minutes
        int hours = zonedDateTime.getHour();
        int minutes = zonedDateTime.getMinute();

        // Format time as "HH.mm"
        return String.format("%02d.%02d", hours, minutes);
    }

    public String convertUnixTimestampToFormattedDate(String unixTimestamp) {
        long milliseconds = Long.parseLong(unixTimestamp) * 1000; // Convert seconds to milliseconds
        Date date = new Date(milliseconds);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        return formatter.format(date);
    }


    private byte[] generateQRCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 2); // Reduce white border
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);

            BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }

            // Convert BufferedImage to byte[]
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating QR Code: " + e.getMessage());
        }
    }

    public Map<String, String> generateUPIQRCode(String orderId, double amount) throws RazorpayException {
        JSONObject qrRequest = new JSONObject();
        qrRequest.put("name", "DATAMAN TEMPLE KIOSK");
        qrRequest.put("usage", "single_use");
        qrRequest.put("fixed_amount", true);
        qrRequest.put("payment_amount", (int) (amount * 100)); // Convert amount to paise
        qrRequest.put("description", "Donation Payment");
        qrRequest.put("order_id", orderId);
        qrRequest.put("type", "upi_qr"); // ✅ Required for UPI QR Code

        // ✅ Generate QR Code
        QrCode qrCode = razorpayClient.qrCode.create(qrRequest);

        // ✅ Extract qr_id from response
        String qrId = qrCode.toJson().getString("id"); // "id" field is the qr_id
        String imageUrl = qrCode.toJson().getString("image_url");

        return Map.of("qr_id", qrId, "image_url", imageUrl);
    }

}
