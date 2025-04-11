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

import dataman.dmbase.dto.RecId;
import dataman.dmbase.server.DmBaseServer;
import in.dataman.transactionDTO.QueueDTO;
import in.dataman.transactionEntity.QueueEntity;
import in.dataman.transactionRepo.QueueRepository;

@Service
public class QueueService {

	@Autowired
	private QueueRepository queueRepository;

	@Autowired
	private DmBaseServer dmBaseServer;

	@Autowired
	@Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public Map<String, String> createQueue(QueueDTO queueDTO) throws Exception {

	    String voucherType = fetchVoucherType("QUE");
	    String vPrefix = fetchVoucherPrefix(queueDTO.getPreparedDt());
	    Long docId = Long.valueOf(dmBaseServer.getDocId(voucherType, vPrefix, "1", jdbcTemplate));

	    RecId recId = dmBaseServer.getRecId("queue", "docId", docId.toString(), "recId", new RecId(),
	            convertUnixTimestampToDate(queueDTO.getPreparedDt()), "v_Type", voucherType, vPrefix, "1", "HO", "1",
	            true, null, jdbcTemplate);

	    // Fetch the allowed number of persons per queue
	    Integer allowedPersons = fetchAllowedPersonPerQueue();

	    // Validate if the entered number of persons exceeds the allowed limit
	    if (Integer.parseInt(queueDTO.getNoOfperson()) > allowedPersons) {
	        return Map.of("Error: You have exceeded the allowed limit of ",allowedPersons.toString());
	    }

	    QueueEntity queueEntity = new QueueEntity();
	    queueEntity.setDocId(docId);
	    queueEntity.setV_Type(Integer.parseInt(voucherType));
	    queueEntity.setV_No(recId.getCounter().intValue());
	    queueEntity.setRecIdPrefix(recId.getPrefix());
	    queueEntity.setRecId(recId.getRecIdValue());
	    queueEntity.setV_Prefix(Integer.parseInt(vPrefix));
	    queueEntity.setV_Date(convertUnixTimestampToDate(queueDTO.getPreparedDt()));
	    queueEntity.setV_Time(Double.parseDouble(convertUnixTimestampToTime(queueDTO.getPreparedDt())));
	    queueEntity.setSite_Code(1);
	    queueEntity.setPreparedBy("Kiosk");
	    queueEntity.setIsdCode("+91");
	    queueEntity.setMobile(queueDTO.getMobile());
	    queueEntity.setNoOfPerson(queueDTO.getNoOfperson());
	    queueEntity.setPreparedDt(convertUnixTimestampToFormattedDate(queueDTO.getPreparedDt()));
	    queueRepository.save(queueEntity);

		Map<String, String> response = new HashMap<>();
		response.put("docId", String.valueOf(docId));
		return response;
	}
	private Integer fetchAllowedPersonPerQueue() {
		String sql = "SELECT e.allowedPersonPerQueue FROM enviro e WHERE e.site_Code = 1";
		return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class)).orElse(0);
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
	
	public Map<String, Object> getQueueDetails(Long docId) {
        String sql = "SELECT q.recId, q.preparedDt, q.mobile, q.noOfPerson " +
                     "FROM queue q WHERE q.docId = ?";

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
        response.put("noOfPerson", firstRow.get("noOfPerson"));

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
