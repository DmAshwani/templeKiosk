package in.dataman.transactionController;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.dataman.transactionEntity.City;
import in.dataman.transactionEntity.Country;
import in.dataman.transactionEntity.State;
import in.dataman.transactionService.LocationService;



@RestController
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
@RequestMapping("/api/v1")
public class LocationController {
	@Autowired
	private LocationService locationService;

//	@Autowired
//	private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

//	@Autowired
//	private AuthKeyUtil authKeyUtil;

	@GetMapping("/countries")
	public ResponseEntity<?> getCountries() {

//		//====================================================================================================================
//		try{
//			authKey = encryptionDecryptionUtil.decrypt(authKey);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		if (authKeyUtil.getAuthKey(authKey) == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
//		}
//
//		authKeyUtil.deleteAuthKey(authKey);
//		HttpHeaders headers = new HttpHeaders();
//		String id = authKeyUtil.generateAuthKey();
//		authKeyUtil.storeAuthKey(id, 60*60*1000);
//		String encryptedAuthKey = null;
//		try{
//			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		headers.add("authKey", encryptedAuthKey);
//		//====================================================================================================================
		List<Country> countries = locationService.getAllCountries();
		//return PayloadEncryptionDecryptionUtil.encryptResponse(countries,encryptionDecryptionUtil);

//		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(countries,encryptionDecryptionUtil);

		return ResponseEntity.ok(countries);
	}

	@GetMapping("/states")
	public ResponseEntity<?> getStates() {


		//====================================================================================================================
//		try{
//			authKey = encryptionDecryptionUtil.decrypt(authKey);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		if (authKeyUtil.getAuthKey(authKey) == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
//		}
//
//		authKeyUtil.deleteAuthKey(authKey);
//		HttpHeaders headers = new HttpHeaders();
//		String id = authKeyUtil.generateAuthKey();
//		authKeyUtil.storeAuthKey(id, 60*60*1000);
//		String encryptedAuthKey = null;
//		try{
//			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================



		List<State> states = locationService.getStatesByCountry();
        //return PayloadEncryptionDecryptionUtil.encryptResponse(states,encryptionDecryptionUtil);

//		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(states,encryptionDecryptionUtil);

		return ResponseEntity.ok(states);

    }

	@GetMapping("/cities")
	public ResponseEntity<?> getCities(@RequestParam String stateCode, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size) {

		//====================================================================================================================
//		try{
//			authKey = encryptionDecryptionUtil.decrypt(authKey);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		if (authKeyUtil.getAuthKey(authKey) == null) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
//		}
//
//		authKeyUtil.deleteAuthKey(authKey);
//		HttpHeaders headers = new HttpHeaders();
//		String id = authKeyUtil.generateAuthKey();
//		authKeyUtil.storeAuthKey(id, 60*60*1000);
//		String encryptedAuthKey = null;
//		try{
//			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================
	
		//return PayloadEncryptionDecryptionUtil.encryptResponse(locationService.getCitiesByState(stateCode, page, size), encryptionDecryptionUtil);
//		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(locationService.getCitiesByState(stateCode, page, size),encryptionDecryptionUtil);

		return ResponseEntity.ok(locationService.getCitiesByState(stateCode, page, size));
	}
	
	@GetMapping("/cities-name")
	public ResponseEntity<?> getCities(
	        @RequestParam String stateCode,
	        @RequestParam(required = false) String namePrefix,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "25") int size) {


		//====================================================================================================================
//		try{
////			authKey = encryptionDecryptionUtil.decrypt(authKey);
////		}catch (Exception e){
////			e.printStackTrace();
////		}
////		if (authKeyUtil.getAuthKey(authKey) == null) {
////			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
////		}
////
////		authKeyUtil.deleteAuthKey(authKey);
////		HttpHeaders headers = new HttpHeaders();
////		String id = authKeyUtil.generateAuthKey();
////		authKeyUtil.storeAuthKey(id, 60*60*1000);
////		String encryptedAuthKey = null;
////		try{
////			encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
////		}catch (Exception e){
////			e.printStackTrace();
////		}
////		headers.add("authKey", encryptedAuthKey);
		//====================================================================================================================


		Page<City> cities = locationService.getCitiesByStateS(stateCode, namePrefix, page, size);


		//return PayloadEncryptionDecryptionUtil.encryptResponse(cities,encryptionDecryptionUtil);

//		Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(cities, encryptionDecryptionUtil);

		return ResponseEntity.ok(cities);
	}

}
