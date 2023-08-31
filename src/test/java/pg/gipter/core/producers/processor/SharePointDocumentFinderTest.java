package pg.gipter.core.producers.processor;

import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class SharePointDocumentFinderTest {

    private SharePointDocumentFinder finder;

    @Test
    void givenSharePointConfigAndDownloadLink_whenCreateDownloadDetails_thenReturnListOfDownloadDetails() {
        SharePointConfig sharePointConfig = new SharePointConfig();
        sharePointConfig.setProject("/cases/GTE440/TOEDNLD");
        sharePointConfig.setUrl("https://netcompany.sharepoint.com");

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.configurationName.name() + "=test"}
        );
        RunConfig runConfig = applicationProperties.getRunConfig("test").orElseGet(RunConfig::new);
        runConfig.addSharePointConfig(sharePointConfig);
        applicationProperties.updateCurrentRunConfig(runConfig);

        Map<String, String> filesToDownload = new HashMap<>();
        filesToDownload.put("fileName", "https://netcompany.sharepoint.com/cases/GTE440/TOEDNLD/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");

        finder = new SharePointDocumentFinder(applicationProperties);

        List<DownloadDetails> actual = finder.createDownloadDetails(
                Collections.singletonList(sharePointConfig), filesToDownload
        );

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getFileName()).isEqualTo("fileName");
        assertThat(actual.get(0).getDownloadLink()).isEqualTo(filesToDownload.get("fileName"));
        assertThat(actual.get(0).getSharePointConfig()).isSameAs(sharePointConfig);
    }
}