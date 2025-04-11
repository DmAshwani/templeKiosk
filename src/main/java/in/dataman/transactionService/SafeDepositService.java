package in.dataman.transactionService;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import dataman.dmbase.dto.RecId;
import dataman.dmbase.server.DmBaseServer;
import in.dataman.transactionDTO.SafeDepositDTO;
import in.dataman.transactionEntity.SafeDepositEntity;
import in.dataman.transactionRepo.SafeDepositRepository;

@Service
public class SafeDepositService {

	@Autowired
	private SafeDepositRepository safeDepositRepository;

	@Autowired
	private DmBaseServer dmBaseServer;

	@Autowired
	@Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
	public Map<String, String> createSafeDeposit(SafeDepositDTO dto) throws Exception {

		String voucherType = fetchVoucherType("SDB");
		String vPrefix = fetchVoucherPrefix(dto.getPreparedDt());
		Long docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, "1", jdbcTemplate));

		RecId recId = dmBaseServer.getRecId("safeDeposit", "docId", docId.toString(), "recId", new RecId(),
				convertUnixTimestampToDate(dto.getPreparedDt()), "v_Type", voucherType, vPrefix, "1", "HO", "1", true,
				null, jdbcTemplate);

		SafeDepositEntity Entity = new SafeDepositEntity();
		Entity.setDocId(docId);
		Entity.setV_Type(Integer.parseInt(voucherType));
		Entity.setV_No(recId.getCounter().intValue());
		Entity.setRecIdPrefix(recId.getPrefix());
		Entity.setRecId(recId.getRecIdValue());
		Entity.setV_Prefix(Integer.parseInt(vPrefix));
		Entity.setV_Date(convertUnixTimestampToDate(dto.getPreparedDt()));
		Entity.setV_Time(Double.parseDouble(convertUnixTimestampToTime(dto.getPreparedDt())));
		Entity.setSite_Code(1);
		Entity.setPreparedBy("Kiosk");
		Entity.setIsdCode("+91");
		Entity.setMobile(dto.getMobile());
		Entity.setNoOfPcs(dto.getNoOfItems());
		Entity.setItemDescription(dto.getItemDescription());
		Entity.setPreparedDt(convertUnixTimestampToFormattedDate(dto.getPreparedDt()));
		safeDepositRepository.save(Entity);

		//return "Successful! You are in the queue.";

		Map<String, String> response = new HashMap<>();
		response.put("docId", String.valueOf(docId));
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

	
	 public Map<String, Object> getSafeDepositDetails(Long docId) {
	        String sql = "SELECT sd.recId, sd.preparedDt, sd.mobile, sd.noOfPcs, sd.itemDescription " +
	                     "FROM safeDeposit sd WHERE sd.docId = ?";

	        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, docId);

	        if (resultList.isEmpty()) {
	            return Collections.emptyMap(); // Return an empty map if no data is found
	        }

	        Map<String, Object> response = new HashMap<>();
	        Map<String, Object> firstRow = resultList.get(0);

	        // Format Date with AM/PM
	        String formattedDate = formatDate(firstRow.get("preparedDt"));

	        response.put("recId", firstRow.get("recId"));
	        response.put("preparedDt", formattedDate);
	        response.put("mobile", firstRow.get("mobile"));
	        response.put("noOfPcs", firstRow.get("noOfPcs"));
	        response.put("itemDescription", firstRow.get("itemDescription"));

	        return response;
	    }

	    private String formatDate(Object dateObj) {
	        if (dateObj == null) {
	            return null;
	        }

	        try {
	            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Assuming SQL format
	            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MMM/yyyy hh:mm a"); // Includes AM/PM
	            return outputFormat.format(inputFormat.parse(dateObj.toString()));
	        } catch (Exception e) {
	            e.printStackTrace();
	            return dateObj.toString(); // Fallback to raw format in case of error
	        }
	    }
	
	
	
}
