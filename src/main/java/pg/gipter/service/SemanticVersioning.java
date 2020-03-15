package pg.gipter.service;

import pg.gipter.utils.StringUtils;

public class SemanticVersioning {
    private int major;
    private int minor;
    private int patch;
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

    public boolean isNewerVersion(SemanticVersioning semanticVersioning) {
        Boolean newVersion = null;
        if (semanticVersioning.getMajor() > getMajor()) {
            newVersion = Boolean.TRUE;
        }
        if (newVersion == null && semanticVersioning.getMinor() > getMinor()) {
            newVersion = Boolean.TRUE;
        }
        if (newVersion == null && semanticVersioning.getPatch() > getPatch()) {
            newVersion = Boolean.TRUE;
        }
        if (newVersion == null) {
            newVersion = StringUtils.notEmpty(semanticVersioning.getAdditionalLabel()) &&
                    !semanticVersioning.getAdditionalLabel().equalsIgnoreCase(getAdditionalLabel());

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
