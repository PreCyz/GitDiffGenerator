package pg.gipter.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/** Created by Pawel Gawedzki on 26-Jul-2019. */
public class ToolkitService {

    protected static Logger logger = LoggerFactory.getLogger(ToolkitService.class);
    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;

    public ToolkitService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpRequester = new HttpRequester(applicationProperties);
    }

    public Set<String> downloadUserProjects() {
        String divIdSelector = "div#MSOZoneCell_WebPartWPQ2";
        String aHrefSelector = "a[href^=" + applicationProperties.toolkitUrl() + "]";
        Set<String> result = new LinkedHashSet<>();
        try {
            String html = httpRequester.downloadPageSource(applicationProperties.toolkitUrl() + "/toolkit/default.aspx");
            Document document = Jsoup.parse(html);
            Element divWithId = document.selectFirst(divIdSelector);
            Elements aElements = divWithId.select(aHrefSelector);
            for (Element a : aElements) {
                result.add(a.attr("href"));
            }
            logger.info("For user [{}] downloaded following projects: [{}]", applicationProperties.toolkitUsername(), result);
        } catch (IOException ex) {
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        }
        return result;
    }
}
