package pg.gipter.service;

import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/** Created by Pawel Gawedzki on 26-Jul-2019. */
public class ToolkitService extends Task<Set<String>> {

    protected static Logger logger = LoggerFactory.getLogger(ToolkitService.class);
    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;

    public ToolkitService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    protected Set<String> call() {
        String divIdSelector = "div#MSOZoneCell_WebPartWPQ2";
        String aHrefSelector = "a[href^=" + applicationProperties.toolkitUrl() + "]";
        Set<String> result = new LinkedHashSet<>();
        try {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloading"));
            String html = httpRequester.downloadPageSource(applicationProperties.toolkitUrl() + "/toolkit/default.aspx");
            if (!StringUtils.notEmpty(html)) {
                throw new IOException("Downloaded source page is empty.");
            }
            Document document = Jsoup.parse(html);
            Element divWithId = document.selectFirst(divIdSelector);
            if (divWithId == null) {
                throw new IOException(String.format("Downloaded source page does not contain element [%s].", divIdSelector));
            }
            Elements aElements = divWithId.select(aHrefSelector);
            if (aElements == null) {
                throw new IOException(String.format("Downloaded source page does not contain element [%s].", aHrefSelector));
            }
            for (Element a : aElements) {
                result.add(a.attr("href"));
            }
            logger.info("For user [{}] following projects were downloaded: [{}].", applicationProperties.toolkitUsername(), result);
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloaded"));
        } catch (IOException ex) {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloadFail"));
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        } finally {
            updateProgress(1, 1);
        }
        return result;
    }

    public Set<String> downloadUserProjects() {
        return call();
    }
}
