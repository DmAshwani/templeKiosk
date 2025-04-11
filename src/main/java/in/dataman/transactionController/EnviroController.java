package in.dataman.transactionController;

import java.util.Optional;

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



    @GetMapping("/enviro")
    public ResponseEntity<Enviro> getEnviroById(@RequestParam Integer id) {
        Optional<Enviro> enviro = enviroService.getEnviroById(id);
        return enviro.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}

