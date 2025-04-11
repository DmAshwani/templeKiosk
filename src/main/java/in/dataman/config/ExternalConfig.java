package in.dataman.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "")
public class ExternalConfig {

    @Value("${sqlHostName}")
    private String sqlHostName;

    @Value("${sqlPort}")
    private String sqlPort;

    @Value("${sqlUser}")
    private String sqlUser;

    @Value("${sqlPassword}")
    private String sqlPassword;

    @Value("${companyDb}")
    private String companyDb;

    @Value("${serverPort}")
    private String serverPort;

    @Value("${serverAddress}")
    private String serverAddress;


//  MongoDB properties
    @Value("${mongoHost}")
    private String mongoHost;

    @Value("${mongoPort}")
    private String mongoPort;

    @Value("${mongoUser}")
    private String mongoUser;

    @Value("${mongoPassword}")
    private String mongoPassword;

    
    @Value("${temRuleCode}")
    private String temRuleCode;
    
    @Value("${redisHost}")
    private String redisHost;

    @Value("${redisPort}")
    private String redisPort;

    
    
  
}
