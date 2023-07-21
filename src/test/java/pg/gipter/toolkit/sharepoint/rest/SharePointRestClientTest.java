package pg.gipter.toolkit.sharepoint.rest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class SharePointRestClientTest {

    @Test
    @Disabled
    void name() {
        String[] strings = {
                ArgName.toolkitUsername.name()+"=PAWG"
        };
        SharePointRestClient client = new SharePointRestClient(ApplicationPropertiesFactory.getInstance(strings));

        try {
            String itemId = client.createItem();
            assertThat(itemId).isNotBlank();
            System.out.println(itemId);

            client.uploadAttachment(itemId);
            client.updateClassificationId(itemId);

        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Should not be any exception here");
        }
    }
}