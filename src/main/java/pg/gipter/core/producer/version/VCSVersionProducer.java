package pg.gipter.core.producer.version;

import java.io.IOException;

public interface VCSVersionProducer {

    String getVersion() throws IOException;

}
