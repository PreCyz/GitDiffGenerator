package pg.gipter.core.dao;

import com.google.gson.Gson;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class MongoConfigTest {

    private CipherDetails loadCipherDetails(String filename) throws Exception {
        try (InputStream fis = new FileInputStream(filename);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            return new Gson().fromJson(reader, CipherDetails.Settings.class).toCipherDetails();
        }
    }

    @Test
    void generate_prodSettings() throws Exception {
        /*System.out.println(new ObjectId().toHexString());
        assertThat(CryptoUtils.decrypt(MongoConfig.password)).isEqualTo("GiptergipteR12");*/

        String password = CryptoUtils.encrypt("Ze1gDFpIH5nkOJNm");
        System.out.println(password);

        CipherDetails cipherDetails = loadCipherDetails("settings-prod.json");
        SecurityService securityService = SecurityService.getInstance();
        String dbHost = securityService.encrypt("db.host=gipter-test-ruzxs.mongodb.net", cipherDetails);
        String dbUsername = securityService.encrypt("db.username=gipter-prod", cipherDetails);
        String dbPassword = securityService.encrypt("db.password=" + password, cipherDetails);
        String dbName = securityService.encrypt("db.dbName=gipter", cipherDetails);

        try (FileWriter fileWriter = new FileWriter("db-prod.connection")) {
            fileWriter.write(dbHost + System.lineSeparator());
            fileWriter.write(dbUsername + System.lineSeparator());
            fileWriter.write(dbPassword + System.lineSeparator());
            fileWriter.write(dbName);
        }

        System.out.println(dbHost);
        System.out.println(dbUsername);
        System.out.println(dbPassword);
        System.out.println(dbName);

    }

    @Test
    void generate_devSettings() throws Exception {
        String password = CryptoUtils.encrypt("mSzEh0sWLEWHVDR6");
        System.out.println(password);

        CipherDetails cipherDetails = loadCipherDetails("settings-dev.json");
        SecurityService securityService = SecurityService.getInstance();
        String dbHost = securityService.encrypt("db.host=gipter-test-ruzxs.mongodb.net/?retryWrites=true&w=majority", cipherDetails);
        String dbUsername = securityService.encrypt("db.username=gipter-test", cipherDetails);
        String dbPassword = securityService.encrypt("db.password=" + password, cipherDetails);
        String dbName = securityService.encrypt("db.dbName=gipter-test", cipherDetails);

        try (FileWriter fileWriter = new FileWriter("db-dev.connection")) {
            fileWriter.write(dbHost + System.lineSeparator());
            fileWriter.write(dbUsername + System.lineSeparator());
            fileWriter.write(dbPassword + System.lineSeparator());
            fileWriter.write(dbName);
        }

        System.out.println(dbHost);
        System.out.println(dbUsername);
        System.out.println(dbPassword);
        System.out.println(dbName);
    }

    @Test
    void generateServiceUserCredentials() {
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setCipherName("PBEwithSHA1AndDESede");
        cipherDetails.setIterationCount(10);
        cipherDetails.setSaltValue("1689777695012");
        cipherDetails.setKeySpecValue("97742dd8-9e89-492a-a2cd-15fee66e64da");

        String username = SecurityService.getInstance().decrypt("shO3hqQWeNHHqw8k1in6+6blFl3dF5de", cipherDetails);
        String password = SecurityService.getInstance().decrypt("0yNi4gQ9qG/2UwMEd+js8Pj0LNhwikMYmpV9/8PuwUqLdlgdQ1OXa26vTUboU87erpbL1GcHxF0=", cipherDetails);
        System.out.println("username: " + username);
        System.out.println("password: " + password);

        username = SecurityService.getInstance().encrypt(username, cipherDetails);
        password = SecurityService.getInstance().encrypt("XS9GupT1Wb84Kg3cinDqJ6dMlwYNRZyxPQvf2C5EVhHjUasmoLzt0IrOFk7eBA", cipherDetails);
        System.out.println("username: " + username);
        System.out.println("password: " + password);

    }

    @Test
    void test1111() throws Exception {
        String salt = UUID.randomUUID().toString();
        String encrypted = encrypt2("lalla", salt);
        System.out.println(encrypted);

        String decrypt = decrypt2(encrypted, salt);
        assertThat(decrypt).isEqualTo("lalla");
    }

    public static String encrypt(String text, String salt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        KeySpec keySpec = new PBEKeySpec("PASSWORD".toCharArray());
        SecretKey key = keyFactory.generateSecret(keySpec);
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt.getBytes(StandardCharsets.UTF_8), 4096, ivSpec);

        Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        byte[] encrypted = cipher.doFinal(text.getBytes());
        return CryptoUtils.base64Encode(encrypted);
    }

    public static String decrypt(String encrypted, String salt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA512AndAES_128");
        KeySpec keySpec = new PBEKeySpec("PASSWORD".toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKey key = keyFactory.generateSecret(keySpec);
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt.getBytes(StandardCharsets.UTF_8), 4096, ivSpec);

        Cipher cipher = Cipher.getInstance("PBEWithHmacSHA512AndAES_128");
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        byte[] decoded = CryptoUtils.base64Decode(encrypted);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private String encrypt2(String text, String salt) throws Exception {
        final String privateKey = UUID.randomUUID().toString();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithHmacSHA512AndAES_128");
        KeySpec spec = new PBEKeySpec(privateKey.toCharArray());
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "PBEWithHmacSHA512AndAES_128");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        PBEParameterSpec pbeSpec = new PBEParameterSpec(salt.getBytes(), 4096, iv);

        Cipher cipher = Cipher.getInstance("PBEWITHHMACSHA512ANDAES_128");
        cipher.init(Cipher.ENCRYPT_MODE, secret, pbeSpec);

        byte[] encrypted = cipher.doFinal(text.getBytes());
        return CryptoUtils.base64Encode(encrypted);
    }

    public String decrypt2(String encrypted, String salt) throws Exception {
        final String privateKey = UUID.randomUUID().toString();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithHmacSHA512AndAES_128");
        KeySpec spec = new PBEKeySpec(privateKey.toCharArray());
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "PBEWithHmacSHA512AndAES_128");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        PBEParameterSpec pbeSpec = new PBEParameterSpec(salt.getBytes(), 4096, iv);

        Cipher cipher = Cipher.getInstance("PBEWithHmacSHA512AndAES_128");
        cipher.init(Cipher.DECRYPT_MODE, secret, pbeSpec);

        byte[] decoded = CryptoUtils.base64Decode(encrypted);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private void sth() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        final String privateKey = UUID.randomUUID().toString();
        final String salt = UUID.randomUUID().toString();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithHmacSHA512AndAES_128");
        KeySpec spec = new PBEKeySpec(privateKey.toCharArray());
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "PBEWithHmacSHA512AndAES_128");
        IvParameterSpec iv = new IvParameterSpec(new byte[16]);
        PBEParameterSpec pbeSpec = new PBEParameterSpec(salt.getBytes(), 4096, iv);

        Cipher cipher = Cipher.getInstance("PBEWITHHMACSHA512ANDAES_128");
        cipher.init(Cipher.ENCRYPT_MODE, secret, pbeSpec);
    }

}