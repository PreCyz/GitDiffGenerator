package pg.gipter.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.rest.SharePointRestClient;

import java.io.IOException;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private static final Logger logger = LoggerFactory.getLogger(DiffUploader.class);

    private final ApplicationProperties applicationProperties;

    public DiffUploader(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void uploadDiff() {
        SharePointRestClient client = new SharePointRestClient(applicationProperties);
        try {
            String itemId = client.createItem();
            client.uploadAttachment(itemId);
            client.updateClassificationId(itemId);
        } catch (IOException e) {
            String errorMsg = String.format("Error during upload diff. %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

}
