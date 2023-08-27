package pg.gipter.ui.alerts;

import pg.gipter.core.dao.gif.CustomGif;
import pg.gipter.core.dao.gif.GifDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Gif {

    private static final List<CustomGif> defaults = Stream.of(
            new CustomGif("BACKPACK_DANCE", "https://media.giphy.com/media/LPkfppCDJOE5rvSCId/giphy.gif", 300, 500, true, false, false),
            new CustomGif("SNAPE_SNAP", "https://media.giphy.com/media/AisOYaOZdrS1i/giphy.gif", 315, 215, false, false, true),
            new CustomGif("PICARD_DAMN", "https://media.giphy.com/media/999fcCCj45Bde/giphy.gif", 265, 205, false, true, false)
    ).collect(Collectors.toList());

    private final CustomGif currentGif;

    private Gif(CustomGif customGif) {
        currentGif = customGif;
    }

    public String url() {
        return currentGif.getUrl();
    }

    public double height() {
        return currentGif.getHeight();
    }

    public double width() {
        return currentGif.getWidth();
    }

    public String name() {
        return currentGif.getName();
    }

    private static Gif randomGif(Collection<CustomGif> set) {
        Random random = new Random();
        int imageIdx = random.nextInt(set.size());
        return new Gif(new ArrayList<>(set).get(imageIdx));
    }

    public static Gif randomPartialSuccessGif() {
        Collection<CustomGif> customGifs = GifDao.readCustomGifs()
                .orElseGet(() -> defaults)
                .stream()
                .filter(CustomGif::isPartialSuccess)
                .collect(Collectors.toList());
        return randomGif(customGifs);
    }

    public static Gif randomSuccessGif() {
        Collection<CustomGif> customGifs = GifDao.readCustomGifs()
                .orElseGet(() -> defaults)
                .stream()
                .filter(CustomGif::isSuccess)
                .collect(Collectors.toList());
        return randomGif(customGifs);
    }

    public static Gif randomFailGif() {
        Collection<CustomGif> customGifs = GifDao.readCustomGifs()
                .orElseGet(() -> defaults)
                .stream()
                .filter(CustomGif::isFail)
                .collect(Collectors.toList());
        return randomGif(customGifs);
    }
}
