package in.dataman.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.SecretKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialDTO {

    private SecretKey secretKey;
    private String authKey;

}
