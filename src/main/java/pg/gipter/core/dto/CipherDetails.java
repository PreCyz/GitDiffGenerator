package pg.gipter.core.dto;

import com.google.gson.annotations.Expose;

import java.nio.charset.StandardCharsets;

public class CipherDetails {

    @Expose
    private String cipher;
    @Expose
    private int iterationCount;
    @Expose
    private String keySpecValue;
    @Expose
    private String saltValue;

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public char[] getKeySpec() {
        return keySpecValue.toCharArray();
    }

    public void setKeySpecValue(String keySpecValue) {
        this.keySpecValue = keySpecValue;
    }

    public byte[] getSalt() {
        return saltValue.substring(0, 8).getBytes(StandardCharsets.UTF_8);
    }

    public String getKeySpecValue() {
        return keySpecValue;
    }

    public String getSaltValue() {
        return saltValue;
    }

    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }
}
