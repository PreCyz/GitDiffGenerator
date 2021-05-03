package pg.gipter.ui.alerts;

import java.util.*;

public enum ImageFile {

    CHICKEN_FACE_PNG("png/chicken-face.png"),
    ERROR_CHICKEN_PNG("png/error-chicken.png"),
    GOOD_JOB_PNG("png/good-job.png"),
    OVERRIDE_PNG("png/override.png"),
    FINGER_UP_PNG("png/finger-up.png"),
    ALMOST_ALL_PNG("png/almost-all.png"),

    MINION_DISCO_GIF("gif/minion-disco.gif"),
    MINION_FART_GIF("gif/minion-fart.gif"),
    MINION_APPLAUSE_GIF("gif/minion-applause.gif"),
    ;

    private final String fileName;

    ImageFile(String fileName) {
        this.fileName = fileName;
    }

    public String fileUrl() {
        return fileName;
    }

    public static ImageFile randomImage(Set<ImageFile> set) {
        Random random = new Random();
        int imageIdx = random.nextInt(set.size());
        return new ArrayList<>(set).get(imageIdx);
    }

    public static ImageFile randomPartialSuccessImage() {
        return randomImage(EnumSet.of(
                ALMOST_ALL_PNG
        ));
    }

    public static ImageFile randomSuccessImage() {
        return randomImage(EnumSet.of(
                GOOD_JOB_PNG, MINION_DISCO_GIF, MINION_APPLAUSE_GIF

        ));
    }

    public static ImageFile randomFailImage() {
        return randomImage(EnumSet.of(
                ERROR_CHICKEN_PNG, MINION_FART_GIF
        ));
    }
}
