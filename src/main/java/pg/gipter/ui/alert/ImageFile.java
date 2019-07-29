package pg.gipter.ui.alert;

public enum ImageFile {

    ERROR_CHICKEN("error-chicken.png"),
    GOOD_JOB("good-job.png"),
    OVERRIDE("override.png"),
    FINGER_UP("finger-up.png"),
    ALMOST_ALL("almost-all.png");

    private String fileName;

    ImageFile(String fileName) {
        this.fileName = fileName;
    }

    String fileName() {
        return fileName;
    }
}
