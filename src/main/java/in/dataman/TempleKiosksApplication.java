package in.dataman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import in.dataman.config.ExternalConfig;

@SpringBootApplication
@EnableConfigurationProperties(ExternalConfig.class)
@ComponentScan(basePackages = {"in.dataman", "dataman.dmbase.encryptiondecryptionutil","dataman.dmbase.server", "dataman.dmbase.encryptiondecryptionutil","dataman.dmbase.documentutil"})
public class TempleKiosksApplication {

	public static void main(String[] args) {
		SpringApplication.run(TempleKiosksApplication.class, args);
	}

}
