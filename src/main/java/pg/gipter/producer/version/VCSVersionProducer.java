package pg.gipter.producer.version;

import java.io.IOException;

public interface VCSVersionProducer {

    String getVersion() throws IOException;

}
