package in.dataman.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class Util {

    @Autowired
    @Qualifier("transactionNamedJdbcTemplate")
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public String getSiteCode() {
        String sql = "SELECT TOP 1 sm.code FROM siteMast sm";
        return namedParameterJdbcTemplate.queryForObject(sql, new HashMap<>(), String.class);
    }

}
