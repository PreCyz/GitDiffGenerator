package pg.gipter.core.producers.vcs;

import java.io.IOException;

public interface VCSVersionProducer {

    String getVersion() throws IOException;

}
