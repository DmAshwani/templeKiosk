package in.dataman.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Temple Kisok API",
        version = "1.0",
        description = "API Documentation for Temple Kisok System",
        contact = @Contact(name = "Support Team", email = "ashwani.pandey@dataman.in")
    )
)
public class SwaggerConfig {
}
