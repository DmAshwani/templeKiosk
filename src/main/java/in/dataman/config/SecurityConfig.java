//package in.dataman.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import in.dataman.companyServ.CustomUserDetailService;
//
//
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//	@Autowired
//	private CustomUserDetailService customUserDetailService;
//
//	@Autowired
//	private JwtAuthenticationEntryPoint authenticationEntryPoint;
//
//	@Autowired
//	private JwtAuthenticationFilter authenticationFilter;
//
//	@Bean
//	PasswordEncoder passwordEncoder() {
//		return new MD5PasswordEncoder(new MD5Util()); // Use the MD5PasswordEncoder with MD5Util
//	}
//
//	@Bean
//	AuthenticationProvider authenticationProvider(MD5Util md5Util) {
//		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//		provider.setUserDetailsService(customUserDetailService);
//		provider.setPasswordEncoder(passwordEncoder());
//		return provider;
//	}
//	
//	 @Bean
//	 AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//	        return authenticationConfiguration.getAuthenticationManager();
//	    }
//
//	@Bean
//	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//		return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable()).authorizeHttpRequests(req -> req
//				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//				.requestMatchers("/api/v1/upload-image","/api/v1/filter" ,"/api/v1/login","/api/v1/save","/api/v1/cities","/api/v1/gateNo","/api/v1/vehicleTypeMast","/api/v1/employee","/swagger-ui/**", "/v3/api-docs/**")
//				.permitAll().anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
//				.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
//				.exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
//				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).build();
//	}
//}
