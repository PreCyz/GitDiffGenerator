package pg.gipter.ui.alerts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public enum ImageFile {

    CHICKEN_FACE_PNG("png/chicken-face.png"),
    ERROR_CHICKEN_PNG("png/error-chicken.png"),
    GOOD_JOB_PNG("png/good-job.png"),
    OVERRIDE_PNG("png/override.png"),
    FINGER_UP_PNG("png/finger-up.png"),
    ALMOST_ALL_PNG("png/almost-all.png"),

    MINION_BOMB_GIF("gif/minion-bomb.gif"),
    MINION_AAAA_GIF("gif/minion-aaaa.gif"),
    MINION_AAAA_2_GIF("gif/minion-aaaa-2.gif"),
    MINION_DISCO_GIF("gif/minion-disco.gif"),
    MINION_FART_GIF("gif/minion-fart.gif"),
    MINION_IOIO_GIF("gif/minion-ioio.gif"),
    MINION_APPLAUSE_GIF("gif/minion-applause.gif"),
    MINION_CROWD_GIF("gif/minion-crowd.gif"),
    BABY_YODA_GIF("gif/baby-yoda.gif"),
    FIRE_BACK_GIF("gif/fire-back.gif"),
    MCGONAGAL_GIF("gif/mcgonagal.gif"),
    SNAPE_GIF("gif/snape.gif"),
    KYLO_REN_PUSH_GIF("gif/kylo_ren_push.gif"),
    KYLO_APPROVES_GIF("gif/kylo_approves.gif"),
    ;

    private String fileName;

    ImageFile(String fileName) {
        this.fileName = fileName;
    }

    public String fileUrl() {
        return fileName;
    }

    private static ImageFile randomImage(Set<ImageFile> set) {
        Random random = new Random();
        int imageIdx = random.nextInt(set.size());
        return new ArrayList<>(set).get(imageIdx);
    }

    public static ImageFile randomPartialSuccessImage() {
        return randomImage(EnumSet.of(
                ALMOST_ALL_PNG, MINION_BOMB_GIF, MINION_FART_GIF, MINION_CROWD_GIF, BABY_YODA_GIF
        ));
    }

    public static ImageFile randomSuccessImage() {
        return randomImage(EnumSet.of(
                GOOD_JOB_PNG, MINION_AAAA_GIF, MINION_DISCO_GIF, MINION_AAAA_2_GIF, MINION_APPLAUSE_GIF, MCGONAGAL_GIF,
                KYLO_APPROVES_GIF
        ));
    }

    public static ImageFile randomFailImage() {
        return randomImage(EnumSet.of(
                ERROR_CHICKEN_PNG, MINION_IOIO_GIF, FIRE_BACK_GIF, SNAPE_GIF, KYLO_REN_PUSH_GIF
        ));
    }
}
