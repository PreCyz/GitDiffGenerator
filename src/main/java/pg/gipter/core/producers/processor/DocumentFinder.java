package pg.gipter.core.producers.processor;

import java.nio.file.Path;
import java.util.List;

public interface DocumentFinder {
    List<Path> find();
}
