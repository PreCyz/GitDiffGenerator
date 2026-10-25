package pg.gipter.ui.alerts;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.gif.CustomGif;
import pg.gipter.core.dao.gif.GifDao;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class GifTest {

//    @Test
//    void name() {
//        List<CustomGif> gifs = Arrays.stream(Gif.values())
//                .map(gif -> new CustomGif(gif.name(),
//                        gif.url(),
//                        gif.height(),
//                        gif.width(),
//                        successGif().contains(gif),
//                        partialSuccessGif().contains(gif),
//                        failGif().contains(gif)))
//                .collect(Collectors.toList());
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(gifs, List.class);
//        try (OutputStream os = new FileOutputStream("gifs.json");
//             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
//        ) {
//            writer.write(json);
//        } catch (IOException e) {
//            throw new IllegalArgumentException("Error when writing configuration into json.");
//        }
//    }

    @Test
    void name2() {
        Optional<List<CustomGif>> customGifs = GifDao.readCustomGifs();
        assertThat(customGifs).isPresent();
        assertThat(customGifs.get()).hasSize(125);
    }
}