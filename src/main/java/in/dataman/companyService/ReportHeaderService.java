package in.dataman.companyService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportHeaderService {

    @Autowired
    @Qualifier("companyJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getReportHeader() {
        String sql = String.format("""
            SELECT 
                CONCAT(plt.comp_Name, ' (', cmp.cYear, ')') AS companyName,
                sm.name,
                CONCAT(sm.address1, ', ', sm.city, ' - ', sm.pin) AS address,
                sm.mobile,
                sm.eMail,
                sm.gstin
            FROM productLicensedTo plt
            LEFT JOIN company cmp ON cmp.pltCode = plt.pltCode
            LEFT JOIN siteMast sm ON sm.code = plt.pltCode
        """);

        System.out.println(sql);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        if (results.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> row = results.get(0);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("companyName", getString(row.get("companyName")));
        resultMap.put("name", getString(row.get("name")));
        resultMap.put("address", getString(row.get("address")));
        resultMap.put("mobile", getString(row.get("mobile")));
        resultMap.put("email", getString(row.get("eMail")));
        resultMap.put("gstin", getString(row.get("gstin")));

        return resultMap;
    }

    private String getString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
