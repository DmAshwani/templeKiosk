package in.dataman.transactionController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = { "http://192.168.6.30:3000" }, originPatterns = "**", allowCredentials = "true")
public class EncryptionDecryptionController {


    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Endpoint to encrypt a JSON request.
     *
     * @param jsonNode The input JSON payload.
     * @return The encrypted cipher as a response.
     */
    @PostMapping("/encrypt-request")
    public ResponseEntity<?> encryptJson(@RequestBody JsonNode jsonNode) {
        try {
            // Convert JsonNode to a string
            String jsonString = objectMapper.writeValueAsString(jsonNode);

            // Encrypt the JSON string
            String encryptedMessage = encryptionDecryptionUtil.encrypt(jsonString);

            HashMap<String, String> response = new HashMap<>();

            response.put("data", encryptedMessage);

            // Return the encrypted string
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error encrypting JSON: " + e.getMessage());
        }
    }

    /**
     * Endpoint to decrypt an encrypted response from a JSON request.
     *
     * @param request The JSON object containing the "encryptedResponse" field.
     * @return The original JSON as a response.
     */
    @PostMapping("/decrypt-request")
    public ResponseEntity<JsonNode> decryptJson(@RequestBody JsonNode request) {
        try {
            // Extract the "encryptedResponse" field from the request
            String encryptedMessage = request.get("data").asText();


            // Decrypt the encrypted message
            String decryptedMessage = encryptionDecryptionUtil.decrypt(encryptedMessage);

            // Convert the decrypted string back to a JsonNode
            JsonNode jsonNode = objectMapper.readTree(decryptedMessage);

            // Return the JSON as the response
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(objectMapper.createObjectNode().put("error", "Error decrypting message: " + e.getMessage()));
        }
    }
}
