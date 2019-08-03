package pg.gipter.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.processor.GETCall;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
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

    public Optional<String> lastItemUploadDate() {
        Optional<String> submissionDate = Optional.empty();

        String select = "$select=Body,SubmissionDate,GUID,Title";
        String orderBy = "$orderby=SubmissionDate+desc";
        String top = "$top=1";
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                select,
                orderBy,
                top
        );
        try {
            JsonObject jsonObject = new GETCall(url, applicationProperties).call();
            if (jsonObject == null) {
                throw new IllegalArgumentException("Null response from toolkit.");
            }
            JsonObject dElement = jsonObject.getAsJsonObject("d");
            if (dElement == null) {
                throw new IllegalArgumentException("Can not handle the response from toolkit.");
            }
            JsonArray results = dElement.getAsJsonArray("results");
            if (results == null || results.size() == 0) {
                throw new IllegalArgumentException("Can not handle the response from toolkit. Array is empty.");
            }
            JsonObject firstElement = results.get(0).getAsJsonObject();
            JsonElement submissionDateElement = firstElement.get("SubmissionDate");
            if (submissionDateElement == null) {
                throw new IllegalArgumentException("Can not find submission date in the response from toolkit.");
            }
            submissionDate = Optional.ofNullable(submissionDateElement.getAsString());
        } catch (Exception ex) {
            logger.error("Can not download last item submission date. ", ex);
        }
        return submissionDate;
    }
}
