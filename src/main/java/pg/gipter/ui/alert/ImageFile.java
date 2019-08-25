package pg.gipter.ui.alert;

public enum ImageFile {

    CHICKEN_FACE_PNG("png/chicken-face.png"),
    ERROR_CHICKEN_PNG("png/error-chicken.png"),
    GOOD_JOB_PNG("png/good-job.png"),
    OVERRIDE_PNG("png/override.png"),
    FINGER_UP_PNG("png/finger-up.png"),
    ALMOST_ALL_PNG("png/almost-all.png"),

    ALMOST_ALL_GIF("gif/almost-all.gif"),
    MINION_AAAA_GIF("gif/minion-aaaa.gif")
    ;

    private String fileName;

    ImageFile(String fileName) {
        this.fileName = fileName;
    }

    public String fileUrl() {
        return fileName;
    }
}
