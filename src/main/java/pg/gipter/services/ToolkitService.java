package pg.gipter.services;

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
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.GETCall;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.users.SuperUserService;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/** Created by Pawel Gawedzki on 26-Jul-2019. */
public class ToolkitService extends Task<Set<String>> {

    protected final static Logger logger = LoggerFactory.getLogger(ToolkitService.class);
    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;
    private final SuperUserService superUserService;

    public ToolkitService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpRequester = new HttpRequester(applicationProperties);
        superUserService = SuperUserService.getInstance();
    }

    @Override
    protected Set<String> call() {
        String divIdSelector = "div#MSOZoneCell_WebPartWPQ2";
        String aHrefSelector = "a[href^=" + applicationProperties.toolkitRESTUrl() + "]";
        Set<String> result = new LinkedHashSet<>();
        try {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloading"));
            SharePointConfig sharePointConfig = new SharePointConfig(
                    superUserService.getUserName(),
                    superUserService.getPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitRESTUrl(),
                    applicationProperties.toolkitRESTUrl() + "/toolkit/default.aspx"
            );

            String html = httpRequester.downloadPageSource(sharePointConfig);
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

    public Optional<String> lastItemModifiedDate(String userId) {
        Optional<String> modifiedDate = Optional.empty();
        if ("".equals(userId)) {
            logger.warn("UserId is empty and I can not download last submission date.");
            return modifiedDate;
        }

        String select = "$select=Body,SubmissionDate,GUID,Title,EmployeeId,Modified";
        String filter = String.format("$filter=EmployeeId+eq+%s", userId);
        String orderBy = "$orderby=Modified+desc";
        String top = "$top=1";
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s&%s",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                select,
                filter,
                orderBy,
                top
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                url
        );

        try {
            JsonObject jsonObject = new GETCall(sharePointConfig, applicationProperties).call();
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
            JsonElement modifiedDateElement = firstElement.get("Modified");
            if (modifiedDateElement == null) {
                throw new IllegalArgumentException("Can not find submission date in the response from toolkit.");
            }
            modifiedDate = Optional.ofNullable(modifiedDateElement.getAsString());
        } catch (Exception ex) {
            logger.error("Can not download last item submission date. {}", ex.getMessage());
        }
        return modifiedDate;
    }

    public boolean hasProperCredentials() {
        boolean result = true;
        String select = "$select=Body,SubmissionDate,GUID,Title";
        String orderBy = "$orderby=SubmissionDate+desc";
        String top = "$top=1";
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                select,
                orderBy,
                top
        );

        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                url
        );
        try {
            result = new GETCall(sharePointConfig, applicationProperties).call() != null;
        } catch (Exception ex) {
            logger.error("Toolkit credentials are not valid. {}", ex.getMessage());
            result = false;
        }

        return result;
    }

}
