package pg.gipter.core.producer.vcs;

import java.io.IOException;

public interface VCSVersionProducer {

    String getVersion() throws IOException;

}
