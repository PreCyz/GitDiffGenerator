package pg.gipter.services;

import pg.gipter.utils.StringUtils;

public class SemanticVersioning {
    private final int major;
    private final int minor;
    private final int patch;
    private String additionalLabel;
    private static final String TAG_SUFFIX = "v";

    public SemanticVersioning(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getAdditionalLabel() {
        return additionalLabel;
    }

    public void setAdditionalLabel(String additionalLabel) {
        this.additionalLabel = additionalLabel;
    }

    public String getVersion() {
        return String.format("%d.%d.%d%s", major, minor, patch, additionalLabel);
    }

    public boolean isNewerVersionThan(SemanticVersioning otherVersion) {
        Boolean newVersion = null;
        if (getMajor() > otherVersion.getMajor()) {
            newVersion = Boolean.TRUE;
        }
        boolean isMajorEquals = getMajor() == otherVersion.getMajor();
        if (newVersion == null && isMajorEquals && getMinor() > otherVersion.getMinor()) {
            newVersion = Boolean.TRUE;
        }
        boolean isMinorEquals = getMinor() == otherVersion.getMinor();
        if (newVersion == null && isMajorEquals && isMinorEquals && getPatch() > otherVersion.getPatch()) {
            newVersion = Boolean.TRUE;
        }
        if (newVersion == null) {
            newVersion = StringUtils.notEmpty(getAdditionalLabel()) &&
                    !getAdditionalLabel().equalsIgnoreCase(otherVersion.getAdditionalLabel());
        }

        return newVersion;
    }

    public static SemanticVersioning getSemanticVersioning(String version) {
        int major = 0;
        int minor = 0;
        int patch = 0;
        String label = "";
        if (version.contains(TAG_SUFFIX)) {
            version = version.substring(version.indexOf(TAG_SUFFIX) + 1);
        }
        String[] split = version.split("\\.");
        if (split.length >= 1) {
            major = Integer.parseInt(split[0]);
        }
        if (split.length >= 2) {
            minor = Integer.parseInt(split[1]);
        }
        if (split.length >= 3) {
            if (split[2].contains("-")) {
                String[] patchSplit = split[2].split("-");
                patch = Integer.parseInt(patchSplit[0]);
                label = patchSplit[1];
            } else {
                patch = Integer.parseInt(split[2]);
            }
        }
        SemanticVersioning semanticVersioning = new SemanticVersioning(major, minor, patch);
        semanticVersioning.setAdditionalLabel(label);
        return semanticVersioning;
    }

    @Override
    public String toString() {
        return "SemanticVersioning{" +
                "major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                ", additionalLabel='" + additionalLabel + '\'' +
                '}';
    }
}
