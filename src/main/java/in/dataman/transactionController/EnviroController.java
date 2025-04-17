package in.dataman.transactionController;

import java.util.Map;
import java.util.Optional;

import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtilNew;
import dataman.dmbase.encryptiondecryptionutil.PayloadEncryptionDecryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.dataman.transactionEntity.Enviro;
import in.dataman.transactionService.EnviroService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class EnviroController {

    @Autowired
    private EnviroService enviroService;


    @Autowired
    private EncryptionDecryptionUtilNew encryptionDecryptionUtil;

    @GetMapping("/enviro")
    public ResponseEntity<?> getEnviroById(@RequestParam Integer id) {
        Optional<Enviro> enviro = enviroService.getEnviroById(id);

        if(enviro.isPresent()){
            Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(enviro.get(), encryptionDecryptionUtil);
            return ResponseEntity.ok(result);
        }
        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse("ENVIRO NOT FOUND", encryptionDecryptionUtil);
        return ResponseEntity.ok(result);
    }

//    @GetMapping("/enviro")
//    public ResponseEntity<?> getEnviroById(@RequestParam Integer id) {
//        Optional<Enviro> enviro = enviroService.getEnviroById(id);
//
//        Map<String, String> result = PayloadEncryptionDecryptionUtil.encryptResponse(enviro.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()), encryptionDecryptionUtil);
//
//        return ResponseEntity.ok(result);
//    }

}

