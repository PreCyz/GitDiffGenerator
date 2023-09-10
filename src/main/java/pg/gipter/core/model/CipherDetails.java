package pg.gipter.core.model;

import org.bson.types.ObjectId;

import java.nio.charset.StandardCharsets;

public class CipherDetails {

    public static final String CIPHER_DETAILS = "cipherDetails";

    private ObjectId id;
    private String cipherName;
    private int iterationCount;
    private String keySpecValue;
    private String saltValue;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCipherName() {
        return cipherName;
    }

    public void setCipherName(String cipherName) {
        this.cipherName = cipherName;
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

    public static class Settings {
        private final String cup;
        private final int laps;
        private final String owner;
        private final String joke;

        public Settings(String cup, int laps, String owner, String joke) {
            this.cup = cup;
            this.laps = laps;
            this.owner = owner;
            this.joke = joke;
        }

        public CipherDetails toCipherDetails() {
            CipherDetails cipherDetails = new CipherDetails();
            cipherDetails.setCipherName(cup);
            cipherDetails.setIterationCount(laps);
            cipherDetails.setKeySpecValue(owner);
            cipherDetails.setSaltValue(joke);
            return cipherDetails;
        }
    }
}
