package in.dataman.transactionService;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.razorpay.QrCode;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import dataman.dmbase.dto.RecId;
import dataman.dmbase.server.DmBaseServer;
import in.dataman.Enums.PaymentMode;
import in.dataman.Enums.PaymentStatus;
import in.dataman.transactionDTO.CompositeKey;
import in.dataman.transactionDTO.Prasad;
import in.dataman.transactionDTO.PrasadBookingDTO;
import in.dataman.transactionEntity.PaymentDetail;
import in.dataman.transactionEntity.SBillDetailEntity;
import in.dataman.transactionEntity.SBillEntity;
import in.dataman.transactionRepo.PaymentDetailRepository;
import in.dataman.transactionRepo.SBillDetailRepository;
import in.dataman.transactionRepo.SBillRepository;

@Service
public class PrasadBookingService {

	@Autowired
	private SBillRepository sBillRepository;

	@Autowired
	private SBillDetailRepository sBillDetailRepository;

	@Autowired
	private PaymentDetailRepository paymentDetailRepository;

	@Autowired
	private RazorpayService razorpayService;

	@Autowired
	private RazorpayClient razorpayClient;

	@Autowired
	private DmBaseServer dmBaseServer;

	@Autowired
	@Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
	public Map<String, String> createPrasadBooking(PrasadBookingDTO dto) {
		Map<String, String> response = new HashMap<>();
		String razorpayOrderId = "";
		Long docId = null;
		try {
			String voucherType = fetchVoucherType("PRB");
			String vPrefix = fetchVoucherPrefix(dto.getPreparedDt());
			docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, "1", jdbcTemplate));

			RecId recId = dmBaseServer.getRecId("sBill", "docId", docId.toString(), "recId", new RecId(),
					convertUnixTimestampToDate(dto.getPreparedDt()), "v_Type", voucherType, vPrefix, "1", "HO", "1",
					true, null, jdbcTemplate);

			// Create and Save SBillEntity
			SBillEntity entity = new SBillEntity();
			entity.setDocId(docId);
			entity.setV_Type(Integer.parseInt(voucherType));
			entity.setV_No(recId.getCounter().intValue());
			entity.setRecIdPrefix(recId.getPrefix());
			entity.setRecId(recId.getRecIdValue());
			entity.setV_Prefix(Integer.parseInt(vPrefix));
			entity.setV_Date(convertUnixTimestampToDate(dto.getPreparedDt()));
			entity.setV_Time(Double.parseDouble(convertUnixTimestampToTime(dto.getPreparedDt())));
			entity.setSite_Code(1);
			entity.setPreparedBy("Kiosk");
			entity.setPartyCode(dto.getPartyCode());
			entity.setDevoteeName(dto.getName());
			entity.setMobile(dto.getMobile());
			entity.setOh_Amt_Gross(dto.getTotal());
			entity.setOh_Amt_Total(dto.getTotal());
			entity.setOh_Amt_Net(dto.getTotal());
			entity.setPreparedDt(convertUnixTimestampToFormattedDate(dto.getPreparedDt()));
			sBillRepository.save(entity);

			// Create and Save SBillDetailEntity
			AtomicInteger counter = new AtomicInteger(1);
			for (Prasad prasadItem : dto.getPrasad()) {
				SBillDetailEntity listEntity = new SBillDetailEntity();
				listEntity.setId(new CompositeKey(docId, String.valueOf(counter.getAndIncrement())));
				listEntity.setItemCode(prasadItem.getCode());
				listEntity.setUnitCode(prasadItem.getSaleUnit());
				listEntity.setOh_Amt_Gross(prasadItem.getAmount());
				listEntity.setOh_Amt_Net(prasadItem.getAmount());
				listEntity.setOh_Amt_Taxable(prasadItem.getAmount());
				listEntity.setQuantity(prasadItem.getQuantity());
				listEntity.setRate(prasadItem.getRate());
				listEntity.setSkuQty(prasadItem.getQuantity());
				listEntity.setSkuRate(prasadItem.getRate());
				sBillDetailRepository.save(listEntity);
			}

			// Generate Razorpay Order
			String orderId = UUID.randomUUID().toString().replace("-", "");
			razorpayOrderId = razorpayService.createOrder(Double.parseDouble(dto.getTotal()), "INR", orderId);
			Map<String, String> qrCodeObj = generateUPIQRCode(razorpayOrderId, Double.parseDouble(dto.getTotal()));

			// Create and Save PaymentDetail
			PaymentDetail paymentDetail = new PaymentDetail();
			paymentDetail.setV_Type(Integer.parseInt(voucherType));
			paymentDetail.setRecId(recId.getRecIdValue());
			paymentDetail.setV_Date(convertUnixTimestampToDate(dto.getPreparedDt()));
			paymentDetail.setV_Time(Double.parseDouble(convertUnixTimestampToTime(dto.getPreparedDt())));
			paymentDetail.setDocId(docId);
			paymentDetail.setSite_Code((short) 1);
			paymentDetail.setPaymentOption(PaymentMode.ONLINE.getCode());
			paymentDetail.setOnlineTransId(orderId);
			paymentDetail.setResTransRefId(razorpayOrderId);
			paymentDetail.setAmount(Double.parseDouble(dto.getTotal()));
			paymentDetail.setStatus(PaymentStatus.AppInitiated.getCode());
			paymentDetail.setPreparedDt(convertUnixTimestampToFormattedDate(dto.getPreparedDt()));
			paymentDetailRepository.save(paymentDetail);

			// Update Payment ID in SBillEntity
			paymentDetailRepository.findByResTransRefId(razorpayOrderId).ifPresent(pd -> {
				entity.setPaymentId(pd.getId().toString());
				sBillRepository.save(entity);
			});

			response.put("qrCode", qrCodeObj.get("image_url"));
			response.put("orderId", razorpayOrderId);
			response.put("upiUrl", qrCodeObj.get("qr_id"));
			response.put("DocId", docId.toString());
		} catch (Exception e) {
			e.printStackTrace(); // Consider using a logger instead
			throw new RuntimeException("Error processing Prasad booking: " + e.getMessage());
		} finally {
			// Any necessary cleanup (not usually needed with Spring Transaction Management)
		}
		return response;
	}

	
	public String cancelPaymentByDocId(String docId, String cancelledDt) {
		Optional<SBillEntity> dot = sBillRepository.findById(Long.parseLong(docId));
		if (dot.isEmpty()) {
			return "No Record Found! "+docId;
		}
		SBillEntity sBill = dot.get();
		sBill.setCancelledBy("Kiosk");
		sBill.setCancelledDt(convertUnixTimestampToFormattedDate(cancelledDt));
		sBill.setPaymentId(null);
		sBillRepository.save(sBill);
		paymentDetailRepository.deleteByDocId(Long.parseLong(docId));
		return "sBill table updated successfully and record deleted from paymentDetail table by docId: "+docId;
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

		long timestamp = Long.parseLong(unixTimestamp);

		Instant instant = Instant.ofEpochSecond(timestamp);

		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		return zonedDateTime.format(formatter);
	}

	public String convertUnixTimestampToTime(String unixTimestamp) {

		long timestamp = Long.parseLong(unixTimestamp);

		Instant instant = Instant.ofEpochSecond(timestamp);

		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

		int hours = zonedDateTime.getHour();
		int minutes = zonedDateTime.getMinute();

		return String.format("%02d.%02d", hours, minutes);
	}

	private String convertUnixTimestampToFormattedDate(String unixTimestamp) {
		long milliseconds = Long.parseLong(unixTimestamp) * 1000; // Convert seconds to milliseconds
		Date date = new Date(milliseconds);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		return formatter.format(date);
	}

	public Map<String, Object> getPrasadBookingDetails(Long docId) {
	    String sql = "SELECT sb.recId, sb.preparedDt, sb.devoteeName, sb.mobile, pd.resbanktransrefNo, " +
	                 "sbd.rate, sbd.quantity, sbd.oh_Amt_Gross AS lineAmt, sb.oh_Amt_Net AS netAmt, " +
	                 "im.name AS itemName, sbd.v_SNo " +
	                 "FROM sBill sb " +
	                 "LEFT JOIN sbilldetail sbd ON sb.docId = sbd.docId " +
	                 "LEFT JOIN itemMast im ON im.code = sbd.itemCode " +
	                 "LEFT JOIN paymentdetail pd ON pd.id = sb.paymentId " +
	                 "WHERE sb.docId = ? " +
	                 "ORDER BY sbd.v_SNo";

	    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, docId);

	    if (resultList.isEmpty()) {
	        return Collections.emptyMap(); // Return an empty map if no data is found
	    }

	    // Initialize the final response map
	    Map<String, Object> response = new HashMap<>();
	    
	    // Extract common fields from the first record
	    Map<String, Object> firstRow = resultList.get(0);
	    
	    String formattedDate = formatDate(firstRow.get("preparedDt"));
	    
	    response.put("recId", firstRow.get("recId"));
	    response.put("v_Date", formattedDate);
	    response.put("devoteeName", firstRow.get("devoteeName"));
	    response.put("mobile", firstRow.get("mobile"));
	    response.put("resbanktransrefNo", firstRow.get("resbanktransrefNo"));

	    // Collect prasad details
	    List<Map<String, Object>> prasadList = new ArrayList<>();
	    for (Map<String, Object> row : resultList) {
	        Map<String, Object> prasadItem = new HashMap<>();
	        prasadItem.put("rate", row.get("rate"));
	        prasadItem.put("quantity", row.get("quantity"));
	        prasadItem.put("lineAmt", row.get("lineAmt"));
	        prasadItem.put("netAmt", row.get("netAmt"));
	        prasadItem.put("itemName", row.get("itemName"));
	        prasadItem.put("v_SNo", row.get("v_SNo"));

	        prasadList.add(prasadItem);
	    }

	    response.put("parssad", prasadList);
	    
	    return response;
	}

	private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Assuming SQL format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm a");
            return outputFormat.format(inputFormat.parse(dateObj.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return dateObj.toString(); // Fallback to raw format in case of error
        }
    }
	
}
