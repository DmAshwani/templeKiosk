package in.dataman.transactionRepo;


import in.dataman.Enums.ProjectItemType;

import in.dataman.Enums.ProjectServiceEvalType;
import in.dataman.Enums.VoucherCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

@Repository
public class PoojaBookingRepository {

    @Autowired
    @Qualifier("transactionNamedJdbcTemplate")
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public List<Map<String, Object>> getPGItems(String category) {


        String sql = """
                SELECT im.code, im.displayName AS name, im.serviceEvalRate AS rate,
                               im.perDayQuota, im.allowedPersonPerBooking, im.serviceNature, im.advanceBookingDays
                               ,serviceTiming AS pujaTiming,im.serviceEvalTypeCode
                        FROM itemMast im
                        LEFT JOIN itemType it ON it.code = im.itemType
                        WHERE it.manualCode = :manualCode AND im.isActive = 1
                        ORDER BY im.displayName
                """;
        Map<String, Object> params = new HashMap<>();

        String poojaBooking = String.valueOf(VoucherCategory.PUJA_BOOKING);

        if(category.equals(poojaBooking)){

            System.out.println("Category Pooja Booking");
            params.put("manualCode", String.valueOf(ProjectItemType.PUJA_GENERAL.getManualCode()));

        }else{

            System.out.println("Category trustee Pooja Booking");
            params.put("manualCode", String.valueOf(ProjectItemType.PUJA_TRUSTEE.getManualCode()));
        }

        System.out.println("manualCode = " + String.valueOf(ProjectItemType.PUJA_GENERAL.getManualCode()));

        return namedParameterJdbcTemplate.query(sql, params, new ItemRowMapper());
    }



    public Map<String, Object> getPoojaDetails(Long itemCode) {
        String sql = """
            SELECT 
                im.code, 
                im.displayName AS name, 
                im.serviceEvalRate AS rate,
                im.perDayQuota, 
                im.allowedPersonPerBooking, 
                im.serviceNature, 
                im.advanceBookingDays,
                im.serviceTiming AS pujaTiming, 
                im.serviceEvalTypeCode  
            FROM itemMast im 
            LEFT JOIN itemType it ON it.code = im.itemType
            WHERE im.isActive = 1
              AND im.code = :itemCode
            ORDER BY im.displayName
        """;


        String poojaBooking = String.valueOf(VoucherCategory.PUJA_BOOKING);

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("itemCode", itemCode);

        return namedParameterJdbcTemplate.query(sql, params, new ItemRowMapper()).get(0);
    }


    private static class ItemRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException, SQLException {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", rs.getString("code"));
            item.put("name", rs.getString("name")); // alias for im.displayName
            item.put("rate", rs.getBigDecimal("rate")); // alias for im.serviceEvalRate
            item.put("perDayQuota", rs.getInt("perDayQuota"));
            item.put("allowedPersonPerBooking", rs.getInt("allowedPersonPerBooking"));
            item.put("serviceType", rs.getString("serviceNature"));
            item.put("advanceBookingDays", rs.getInt("advanceBookingDays"));
            item.put("pujaTiming", new DecimalFormat("00.00").format(rs.getDouble("pujaTiming")));
            item.put("serviceEvalTypeCode", rs.getInt("serviceEvalTypeCode"));
            item.put("serviceEvalShortName", ProjectServiceEvalType.getShortNameByCode( rs.getInt("serviceEvalTypeCode")));
            return item;
        }
    }




    public List<Map<String, Object>> getBookingSummaryAfterDate(Long itemCode, LocalDate vDate) {

        System.out.println(vDate);

        String sql = """
        SELECT
            serviceDate,
            totalBooking
        FROM serviceBookingDateWiseSummary
        WHERE itemCode = :itemCode
          AND serviceDate >= :vDate
        ORDER BY serviceDate
        """;


        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("itemCode", itemCode)
                .addValue("vDate", vDate);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();

            row.put("vDate", rs.getTimestamp("serviceDate")); // returns java.sql.Timestamp



            Object quotaObj = getPoojaDetails(itemCode).get("perDayQuota");
            int perDayQuota = quotaObj instanceof Integer ? (Integer) quotaObj : Integer.parseInt(quotaObj.toString());
            row.put("availability", perDayQuota - rs.getInt("totalBooking"));
            //row.put("availability", rs.getInt("totalBooking"));

            System.out.println("Filtered Row  "+row);
            return row;
        });
    }




    public Map<String, LocalDate> getAllowedBookingDates(Long itemCode) {

        String query = """
        SELECT os.fromDate, os.toDate
        FROM occasionSchedule os
        LEFT JOIN occasionScheduleDetail osd ON os.docId = osd.docId
        LEFT JOIN occasionMast om ON om.code = os.occasionCode
        WHERE osd.itemCode = :itemCode AND osd.isAllowedBooking = 0 AND om.isActive = 1
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("itemCode", itemCode);

        // Fetch all blocked ranges
        List<Map<String, LocalDate>> blockedDateRanges = namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> {
            Map<String, LocalDate> range = new HashMap<>();
            range.put("fromDate", rs.getDate("fromDate").toLocalDate());
            range.put("toDate", rs.getDate("toDate").toLocalDate());
            return range;
        });


        if(blockedDateRanges.isEmpty()){
            return null;
        }

        return blockedDateRanges.get(0);

    }

    public List<LocalDate> getExclusionDate(LocalDate filterFromDate, Long itemCode) {

        Map<String, LocalDate> dateRage = getAllowedBookingDates(itemCode);

        if(dateRage == null){
            return null;
        }

        LocalDate startDate = dateRage.get("fromDate");
        LocalDate endDate = dateRage.get("toDate");

        String sql = """
        WITH DateRange AS (
            SELECT CAST(:startDate AS DATE) AS dt
            UNION ALL
            SELECT DATEADD(DAY, 1, dt)
            FROM DateRange
            WHERE dt < :endDate
        )
        SELECT dt
        FROM DateRange
        WHERE dt >= :filterFromDate
        OPTION (MAXRECURSION 1000)
        """;

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("filterFromDate", filterFromDate);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getDate("dt").toLocalDate());
    }


    public Integer getTotalBooking(long itemCode, int siteCode, String vDate) {
        String sql = "SELECT totalBooking FROM serviceBookingDateWiseSummary " +
                "WHERE itemCode = :itemCode AND site_Code = :siteCode AND serviceDate = :vDate";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("itemCode", itemCode);
        parameters.addValue("siteCode", siteCode);
        parameters.addValue("vDate", vDate);

        List<Integer> results = namedParameterJdbcTemplate.query(sql, parameters,
                (rs, rowNum) -> rs.getInt("totalBooking"));

        return results.isEmpty() ? 0 : results.get(0);
    }




    public int cancelBooking(Long docId, String cancelledDt) {
        String sql = "UPDATE dbo.serviceBooking " +
                "SET cancelledBy = :cancelledBy, " +
                "    cancelledDt = :cancelledDt, " +
                "    paymentId = NULL " +
                "WHERE docId = :docId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("cancelledBy", "kiosk");
        params.addValue("cancelledDt", cancelledDt);
        params.addValue("docId", docId);

        return namedParameterJdbcTemplate.update(sql, params);
    }

    public Integer getAdvanceBookingDaysByCode(String code) {
        String sql = "SELECT advanceBookingDays FROM itemMast WHERE code = :code";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", code);

        return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
    }


    public Optional<Map<String, Object>> getBookingSummaryByDocId(Long docId) {
        String sql = """
        SELECT sb.site_Code, sb.itemCode, sb.serviceDate, sb.noOfBooking AS transBooking, sb.cancelledBy,
               im.perDayQuota AS mastPerDayQuota, sbds.totalBooking, sbds.isStatus
        FROM serviceBooking sb
        LEFT JOIN itemMast im ON im.code = sb.itemCode
        LEFT JOIN serviceBookingDateWiseSummary sbds ON sbds.site_Code = sb.site_Code
                                                      AND sbds.itemCode = sb.itemCode
                                                      AND sbds.serviceDate = sb.serviceDate
        WHERE sb.docId = :docId AND sbds.itemCode IS NOT NULL
    """;

        System.out.println("Sql Query "+ sql);
        System.out.println("docid in repo function "+docId);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("docId", docId);

        List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(sql, params);

        System.out.println("Query "+sql);

        System.out.println("Result List   "+resultList);

        if (resultList.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> row = resultList.get(0);
        Map<String, Object> response = new HashMap<>();

        // Manually set each key with custom names (camelCase style)
        response.put("siteCode", row.get("site_Code"));
        response.put("itemCode", row.get("itemCode"));
        response.put("serviceDate", row.get("serviceDate"));
        response.put("transBooking", row.get("transBooking"));
        response.put("cancelledBy", row.get("cancelledBy"));
        response.put("mastPerDayQuota", row.get("mastPerDayQuota"));
        response.put("totalBooking", row.get("totalBooking"));
        response.put("isStatus", row.get("isStatus"));

        return Optional.of(response);
    }


    public int getBookingSummaryCount(int siteCode, Long itemCode, String date) {
        String sql = """
        SELECT COUNT(*) AS cnt
        FROM serviceBookingDateWiseSummary sbds
        WHERE sbds.site_Code = :siteCode
          AND sbds.itemCode = :itemCode
          AND sbds.serviceDate = :vDate
    """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("siteCode", siteCode);
        params.addValue("itemCode", itemCode);
        params.addValue("vDate", date); // assuming v_Date is of type DATE

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);

        return Optional.ofNullable(count).orElse(0);
    }





}
