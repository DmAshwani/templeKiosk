package in.dataman.transactionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import in.dataman.Enums.ProjectItemType;

@Service
public class ItemService {

	@Autowired
	@Qualifier("TransactionJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("transactionNamedJdbcTemplate")
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public List<Map<String, Object>> getItems() {
	    String sql = """
	            SELECT im.code, im.displayName AS name, im.serviceEvalRate AS rate, im.saleUnit
	            FROM itemMast im
	            LEFT JOIN itemType it ON it.code = im.itemType
	            WHERE it.manualCode = :manualCode AND im.isActive = 1
	            ORDER BY im.displayName
	        """;

	    Map<String, Object> params = Map.of("manualCode", ProjectItemType.FINISHED_MTRL.getManualCode());

	    List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(sql, params);

	    List<Map<String, Object>> simplifiedList = new ArrayList<>();

	    for (Map<String, Object> row : resultList) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("code", row.get("code"));
	        map.put("name", row.get("name")); // alias for displayName
	        map.put("rate", row.get("rate"));
	        map.put("saleUnit", row.get("saleUnit"));
	        simplifiedList.add(map);
	    }

	    return simplifiedList;
	}



	public List<Map<String, Object>> getPGItem() {
	    String sql = """
	                SELECT im.code, im.displayName AS name, im.serviceEvalRate AS rate,
	                       im.perDayQuota, im.allowedPersonPerBooking, im.serviceType, im.advanceBookingDays
	                FROM itemMast im
	                LEFT JOIN itemType it ON it.code = im.itemType
	                WHERE it.manualCode = :manualCode AND im.isActive = 1
	                ORDER BY im.displayName
	            """;

	    Map<String, Object> params = Map.of("manualCode", ProjectItemType.PUJA_GENERAL.getManualCode());

	    List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(sql, params);
	    
	    List<Map<String, Object>> simplifiedList = new ArrayList<>();

	    for (Map<String, Object> row : resultList) {
	        Map<String, Object> map = new HashMap<>();
	        map.put("code", row.get("code"));
	        map.put("name", row.get("name"));
	        map.put("rate", row.get("rate"));
	        map.put("perDayQuota", row.get("perDayQuota"));
	        map.put("allowedPersonPerBooking", row.get("allowedPersonPerBooking"));
	        map.put("serviceType", row.get("serviceType"));
	        map.put("advanceBookingDays", row.get("advanceBookingDays"));
	        simplifiedList.add(map);
	    }

	    return simplifiedList;
	}


}
