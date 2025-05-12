package in.dataman.util;

import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class EncryptionDecryptionUtilityClass {

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    public CredentialDTO getCredentials(){
        SecretKey secretKey = encryptionDecryptionUtil.getSecretKey();
        CredentialDTO credentialDTO = new CredentialDTO();
        credentialDTO.setSecretKey(secretKey);

        return credentialDTO;
    }

}
