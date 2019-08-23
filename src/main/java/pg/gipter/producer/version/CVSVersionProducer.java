package pg.gipter.producer.version;

import java.io.IOException;

public interface CVSVersionProducer {

    String getVersion() throws IOException;

}
