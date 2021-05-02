package pg.gipter.ui.alerts;

import java.util.*;

public enum Gif {
    BACKPACK_DANCE("https://media.giphy.com/media/LPkfppCDJOE5rvSCId/giphy.gif", 500, 300),
    NEVIL_FINIGAN_JOY("https://media.giphy.com/media/bCYK1Cmi1VCso/giphy.gif", 225, 495),
    SNAPE_SNAP("https://media.giphy.com/media/AisOYaOZdrS1i/giphy.gif", 215, 315),
    PICARD_DAMN("https://media.giphy.com/media/999fcCCj45Bde/giphy.gif", 205, 265),
    TUK_TUK("https://media.giphy.com/media/icZdyW06hn0js2OeOL/giphy.gif", 290, 500),
    MINION_IOIO("https://media.giphy.com/media/YAlhwn67KT76E/giphy.gif", 220, 220),
    MINION_FART("https://media.giphy.com/media/fnkyJXcCXZngY/giphy.gif", 220, 260),
    MINION_NAKED_GRU("https://media.giphy.com/media/Q7omesiu4NYzeM9LPT/giphy.gif", 290, 500),
    FROZEN_ANNA_JOY("https://media.giphy.com/media/lwXbNSQr4QEtq/giphy.gif", 180, 260),
    SVEN("https://media.giphy.com/media/VECTSz9Kc7aCs/giphy.gif", 220, 180),
    OLAF("https://media.giphy.com/media/nIWSZ6Pukcnbq/giphy.gif", 200, 420),
    MOANA_BABY("https://media.giphy.com/media/H68cGIIrUQVHO/giphy.gif", 230, 520),
    MAUI_TATOO("https://media.giphy.com/media/l3vRiFOixCouKnAxa/giphy.gif", 220, 490),
    MAUI_OH("https://media.giphy.com/media/l0MYEF46L9RcroEOk/giphy.gif", 220, 495),
    MOANA_MAUI("https://media.giphy.com/media/3oz8xZfA4u5TCk9Esw/giphy.gif", 230, 495),
    MAUI_ANGRY("https://media.giphy.com/media/mQr6BVw0J0PbW/giphy.gif", 260, 350),
    CHUCK_NORRIS_OK("https://media.giphy.com/media/qanrUMM3x50mA/giphy.gif", 335, 500),
    CHUCK_NORRIS_FIST("https://media.giphy.com/media/l1J3G5lf06vi58EIE/giphy.gif", 325, 460),
    SPIDER_MAN("https://media.giphy.com/media/SF9Z0shNT07T2/giphy.gif", 180, 180),
    ASOKA_TANO("https://media.giphy.com/media/26hp5qT9t94h8HkFW/giphy.gif", 290, 500),
    PALPATINE("https://media.giphy.com/media/3ornk7nts29Am5LIfm/giphy.gif", 235, 470),
    MISSED_KICK("https://media.giphy.com/media/KGTTNpVuGVhN6/giphy.gif", 190, 190),
    BARNEY_STINSON_GUN("https://media.giphy.com/media/jSxK33dwEMbkY/giphy.gif", 190, 280),
    BARNEY_STINSON_AAHH("https://media.giphy.com/media/TK3vwIvfBULfO/giphy.gif", 200, 260),
    BARNEY_STINSON_DANCE("https://media.giphy.com/media/kJGLD1ctyrlqU/giphy.gif", 360, 220),
    TED_MARSHAL("https://media.giphy.com/media/Zh0ukrpK77Tos/giphy.gif", 305, 510),
    KYLO_REN("https://media.giphy.com/media/sgWHkkjwcnKes/giphy.gif", 230, 510),
    BAYERN_MUNICH_CELEBRATING("https://media.giphy.com/media/BkwEoWNhSQL9bJc4TH/giphy.gif", 290, 510),
    LEWY_NEUER("https://media.giphy.com/media/qOlfLk1yTKWDti7jdI/giphy.gif", 500, 510),
    VOLDEMORT("https://media.giphy.com/media/wLBS2GlPDALS0/giphy.gif", 200, 260),
    ACE_VENTURA_SUCKS("https://media.giphy.com/media/VQxdDzvRoEwrm/giphy.gif", 370, 480),
    ACE_VENTURA_AJAJAJ("https://media.giphy.com/media/Qs1aHvvivlnm8/giphy.gif", 370, 480),
    ACE_VENTURA_AAAA("https://media.giphy.com/media/EOWVPMCalaDpm/giphy.gif", 290, 230),
    CHICKENS("https://media.giphy.com/media/24FMDDRwtF4uaMTmEQ/giphy.gif", 420, 430),
    YODA_DANCING("https://media.giphy.com/media/6fScAIQR0P0xW/giphy.gif", 210, 170)
    ;

    private final String url;
    private final double height;
    private final double width;

    Gif(String url, double height, double width) {
        this.url = url;
        this.height = height;
        this.width = width;
    }

    public String url() {
        return url;
    }

    public double height() {
        return height;
    }

    public double width() {
        return width;
    }

    private static Gif randomGif(Set<Gif> set) {
        Random random = new Random();
        int imageIdx = random.nextInt(set.size());
        return new ArrayList<>(set).get(imageIdx);
    }

    public static Gif randomPartialSuccessGif() {
        return randomGif(EnumSet.of(
                PICARD_DAMN, TUK_TUK, MINION_FART, SVEN, MOANA_BABY, MAUI_OH, CHUCK_NORRIS_OK, SPIDER_MAN, TED_MARSHAL,
                VOLDEMORT, ACE_VENTURA_AJAJAJ, CHICKENS
        ));
    }

    public static Gif randomSuccessGif() {
        return randomGif(EnumSet.of(
                NEVIL_FINIGAN_JOY, BACKPACK_DANCE, MINION_NAKED_GRU, FROZEN_ANNA_JOY, MAUI_TATOO, PALPATINE,
                BARNEY_STINSON_DANCE, BAYERN_MUNICH_CELEBRATING, LEWY_NEUER, YODA_DANCING
        ));
    }

    public static Gif randomFailGif() {
        return randomGif(EnumSet.of(
                SNAPE_SNAP, MINION_IOIO, OLAF, MOANA_MAUI, MAUI_ANGRY, CHUCK_NORRIS_FIST, ASOKA_TANO, MISSED_KICK,
                BARNEY_STINSON_GUN, BARNEY_STINSON_AAHH, KYLO_REN, ACE_VENTURA_SUCKS, ACE_VENTURA_AAAA
        ));
    }
}
