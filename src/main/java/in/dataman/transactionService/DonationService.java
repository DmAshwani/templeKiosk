package in.dataman.transactionService;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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
import in.dataman.transactionDTO.DonationDTO;
import in.dataman.transactionEntity.Donation;
import in.dataman.transactionEntity.PaymentDetail;
import in.dataman.transactionRepo.DonationRepository;
import in.dataman.transactionRepo.PaymentDetailRepository;

@Service
public class DonationService {

	private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

	private final DonationRepository donationRepository;
	private final PaymentDetailRepository paymentDetailRepository;
	private final RazorpayService razorpayService;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	private DmBaseServer dmBaseServer;

	@Autowired
	private RazorpayClient razorpayClient;

	public DonationService(DonationRepository donationRepository, PaymentDetailRepository paymentDetailRepository,
			RazorpayService razorpayService, @Qualifier("TransactionJdbcTemplate") JdbcTemplate jdbcTemplate,
			DmBaseServer dmBaseServer) {
		this.donationRepository = donationRepository;
		this.paymentDetailRepository = paymentDetailRepository;
		this.razorpayService = razorpayService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
	public Map<String, String> donation(DonationDTO donationDTO) throws RazorpayException {
		try {

			String voucherType = fetchVoucherType("DO");
			String vPrefix = fetchVoucherPrefix(donationDTO.getPreparedDt());
			Long docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, "1", jdbcTemplate));

			RecId recId = dmBaseServer.getRecId("donation", "docId", docId.toString(), "recId", new RecId(),
					convertUnixTimestampToDate(donationDTO.getPreparedDt()), "v_Type", voucherType, vPrefix, "1", "HO",
					"1", true, null, jdbcTemplate);
			String amount = donationDTO.getDonationAmount();

			Donation donation = donationDTO.toDonation();
			donation.setDocId(docId);
			donation.setV_Type(Integer.parseInt(voucherType));
			donation.setV_No(recId.getCounter().intValue());
			donation.setRecIdPrefix(recId.getPrefix());
			donation.setRecId(recId.getRecIdValue());
			donation.setV_Prefix(Integer.parseInt(vPrefix));
			donation.setV_Time(Double.parseDouble(convertUnixTimestampToTime(donationDTO.getPreparedDt())));
			donation.setSite_Code(1);
			donation.setPreparedBy("kiosk");
			donation.setPaymentMode(PaymentMode.ONLINE.getCode());
			donation.setPreparedDt(convertUnixTimestampToFormattedDate(donationDTO.getPreparedDt()));
			donation.setV_Date(convertUnixTimestampToDate(donationDTO.getPreparedDt()));

			donation.setAmount(amount);
			donation.setStatus(PaymentStatus.AppInitiated.getCode());

			donationRepository.save(donation);

			String orderId = UUID.randomUUID().toString().replace("-", "");
			String razorpayOrderId = razorpayService.createOrder(Double.parseDouble(amount), "INR", orderId);

			Map<String, String> qrCodeObj = generateUPIQRCode(razorpayOrderId, Double.parseDouble(amount));

			PaymentDetail paymentDetail = new PaymentDetail();
			paymentDetail.setV_Type(Integer.parseInt(voucherType));
			paymentDetail.setRecId(recId.getRecIdValue());
			paymentDetail.setV_Date(convertUnixTimestampToDate(donationDTO.getPreparedDt()));
			paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(donationDTO.getPreparedDt())));
			paymentDetail.setDocId(docId);
			paymentDetail.setSite_Code((short) 1);
			paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
			paymentDetail.setOnlineTransId(orderId);
			paymentDetail.setResTransRefId(razorpayOrderId);
			paymentDetail.setAmount(Double.parseDouble(amount));
			paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
			paymentDetail.setPreparedDt(convertUnixTimestampToFormattedDate(donationDTO.getPreparedDt()));
			paymentDetailRepository.save(paymentDetail);

			Optional<PaymentDetail> pdoptional = paymentDetailRepository.findByResTransRefId(razorpayOrderId);

			pdoptional.ifPresent(pd -> {
				donation.setPaymentId(pd.getId());
				donationRepository.save(donation);
			});

			Map<String, String> response = new HashMap<>();
			response.put("qrCode", qrCodeObj.get("image_url"));
			response.put("orderId", razorpayOrderId);
			response.put("upiUrl", qrCodeObj.get("qr_id"));
			response.put("DocId", docId.toString());
			return response;
		} catch (Exception ex) {
			logger.error("Error processing donation: {}", ex.getMessage(), ex);
			throw new RuntimeException("Donation processing failed", ex);
		}
	}

	public String cancelPaymentByDocId(String docId, String cancelledDt) {
		Optional<Donation> dot = donationRepository.findById(Long.parseLong(docId));
		if (dot.isEmpty()) {
			return "No Record Found! "+docId;
		}
		Donation donation = dot.get();
		donation.setCancelledBy("Kiosk");
		donation.setCancelledDt(convertUnixTimestampToFormattedDate(cancelledDt));
		donation.setPaymentId(null);
		donationRepository.save(donation);
		paymentDetailRepository.deleteByDocId(Long.parseLong(docId));
		return "Donation table updated successfully and record deleted from paymentDetail table by docId: "+docId;
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

	private String convertUnixTimestampToFormattedDate(String unixTimestamp) {
		long milliseconds = Long.parseLong(unixTimestamp) * 1000; // Convert seconds to milliseconds
		Date date = new Date(milliseconds);

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		return formatter.format(date);
	}

	@SuppressWarnings("unused")
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

	@Autowired
	@Qualifier("transactionNamedJdbcTemplate")
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public Map<String, Object> getDonations(Long docId) {
	    String sql = "SELECT d.preparedDt, d.name AS donor, d.mobile, d.amount, d.recId, " +
	                 "pd.resBankTransrefNo AS transactionId " +
	                 "FROM donation d " +
	                 "LEFT JOIN paymentDetail pd ON d.docId = pd.docId AND pd.id = d.paymentId " +
	                 "WHERE d.docId = :docId";

	    Map<String, Object> params = new HashMap<>();
	    params.put("docId", docId);

	    List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(sql, params);

	    if (resultList.isEmpty()) {
	        return Collections.emptyMap();
	    }

	    Map<String, Object> firstRow = resultList.get(0);
	    Map<String, Object> response = new LinkedHashMap<>();

	    String formattedDate = formatDate(firstRow.get("preparedDt"));

	    response.put("Date & Time", formattedDate);
	    response.put("Donor", firstRow.get("donor")); 
	    response.put("Mobile", firstRow.get("mobile"));
	    response.put("Amount", firstRow.get("amount"));
	    response.put("Transaction Id", firstRow.get("transactionId"));
		response.put("recId", firstRow.get("recId"));

	    return response;
	}

	private String formatDate(Object dateObj) {
	    if (dateObj == null) {
	        return null;
	    }

	    try {
	        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm a");
	        return outputFormat.format(inputFormat.parse(dateObj.toString()));
	    } catch (Exception e) {
	        e.printStackTrace();
	        return dateObj.toString(); // Fallback
	    }
	}

}
