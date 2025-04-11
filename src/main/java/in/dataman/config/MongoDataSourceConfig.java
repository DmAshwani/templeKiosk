//package in.dataman.config;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.MongoDatabaseFactory;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.data.mongodb.gridfs.GridFsTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import com.mongodb.client.MongoClients;
//
//import dataman.dmbase.documentutil.DocumentUtil;
//
//import dataman.dmbase.debug.*;
//
//@Configuration
//public class MongoDataSourceConfig {
//	
//	@Autowired
//	private ExternalConfig externalConfig;
//	
//	@Autowired
//	@Qualifier("companyJdbcTemplate")
//	private JdbcTemplate jdbcTemplate;
//	
//	
//	@Bean
//    public DocumentUtil documentUtil() {
//		
//		
//
//        String mongoDatabase = getCentralFileDataPath();
//        String mongoHost = externalConfig.getMongoHost();
//        String mongoUser = externalConfig.getMongoUser();
//        String mongoPort = externalConfig.getMongoPort();
//        String mongoPassword = externalConfig.getMongoPassword();
//
//        
//        System.out.println(mongoPassword);
//        System.out.println(mongoDatabase);
//        System.out.println(mongoUser);
//        System.out.println(mongoHost);
//        System.out.println(mongoPort);
//       
//
//        String encodedPassword = URLEncoder.encode(mongoPassword, StandardCharsets.UTF_8);
//
//        //String databaseName = "blm";
//
////        String mongoUri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=admin",
////                "dataman", encodedPassword, "javaserver", 27017, mongoDatabase);
//
//        String mongoUri = String.format("mongodb://%s:%s@%s:%s/%s?authSource=admin",
//                mongoUser, encodedPassword, mongoHost, mongoPort, mongoDatabase);
//
//        
//
//        // Create MongoDatabaseFactory
//        MongoDatabaseFactory mongoDatabaseFactory = new SimpleMongoClientDatabaseFactory(MongoClients.create(mongoUri), mongoDatabase);
//
//        // Create MongoTemplate
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory);
//
//        // Create GridFsTemplate
//        GridFsTemplate gridFsTemplate = new GridFsTemplate(mongoDatabaseFactory, mongoTemplate.getConverter());
//
//        // Return DocumentUtil with dependencies
//        return new DocumentUtil(mongoDatabaseFactory, mongoTemplate);
//    }
//	
//	public String getCentralFileDataPath() {
//        String sql = "SELECT cmp.centralFileData_Path " +
//                     "FROM productLicensedto plt " +
//                     "LEFT JOIN company cmp ON cmp.comp_Code = plt.latestCompCode";
//        
//        return jdbcTemplate.queryForObject(sql, String.class);
//    }
//
//    
//}
