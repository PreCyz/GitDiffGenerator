package pg.gipter.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.GETCall;
import pg.gipter.services.dto.ItemField;
import pg.gipter.services.dto.SortFieldDefinition;
import pg.gipter.services.dto.ToolkitCasePayload;
import pg.gipter.services.dto.ToolkitCaseResponse;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.toolkit.sharepoint.HttpRequesterNTML;
import pg.gipter.users.SuperUserService;
import pg.gipter.utils.BundleUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Created by Pawel Gawedzki on 26-Jul-2019. */
public class ToolkitService extends Task<Set<String>> {

    protected final static Logger logger = LoggerFactory.getLogger(ToolkitService.class);
    private final ApplicationProperties applicationProperties;
    private final HttpRequesterNTML httpRequesterNTML;
    private final SuperUserService superUserService;

    public ToolkitService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpRequesterNTML = new HttpRequesterNTML(applicationProperties);
        superUserService = SuperUserService.getInstance();
    }

    @Override
    protected Set<String> call() {
        return getAvailableCases();
    }

    private Set<String> getAvailableCases() {
        CookiesService cookiesService = new CookiesService(applicationProperties);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put("Cookie", cookiesService.getFedAuthString());
        String url = applicationProperties.toolkitHostUrl() + "/_goapi/UserProfile/Cases";
        Set<String> cases = new HashSet<>();
        try {
            ToolkitCasePayload payload = new ToolkitCasePayload(
                    new SortFieldDefinition("ows_Created", "datetime"),
                    List.of(
                            new ItemField("title", "", "ows_Title"),
                            new ItemField("id", "", "CaseID"),
                            new ItemField("created", "", "ows_Created")
                    ),
                    false
            );
            ToolkitCaseResponse response = httpRequesterNTML.post(url, headers, payload, ToolkitCaseResponse.class);
            cases = response.cases.stream().map(it -> it.id).collect(Collectors.toSet());
        } catch (IOException ex) {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloadFail"));
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        } finally {
            updateProgress(1, 1);
        }
        return cases;
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
            JsonObject jsonObject = new GETCall(sharePointConfig, new HttpRequesterNTML(applicationProperties)).call();
            if (jsonObject == null) {
                throw new IllegalArgumentException("Null response from toolkit.");
            }
            JsonObject dElement = jsonObject.getAsJsonObject("d");
            if (dElement == null) {
                throw new IllegalArgumentException("Can not handle the response from toolkit.");
            }
            JsonArray results = dElement.getAsJsonArray("results");
            if (results == null || results.isEmpty()) {
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
            result = new GETCall(sharePointConfig, new HttpRequesterNTML(applicationProperties)).call() != null;
        } catch (Exception ex) {
            logger.error("Toolkit credentials are not valid. {}", ex.getMessage());
            result = false;
        }

        return result;
    }

    public boolean isCookieWorking(String fedAuthString) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put("Cookie", fedAuthString);
        String url = applicationProperties.toolkitHostUrl() + "/_goapi/UserProfile/Cases";
        ToolkitCasePayload payload = new ToolkitCasePayload(
                new SortFieldDefinition("ows_Created", "datetime"),
                List.of(
                        new ItemField("title", "", "ows_Title"),
                        new ItemField("id", "", "CaseID"),
                        new ItemField("created", "", "ows_Created")
                ),
                false
        );
        try {
            int statusCode = httpRequesterNTML.postForStatusCode(url, headers, payload);
            return Stream.of(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .noneMatch(sc -> sc == statusCode);
        } catch (IOException ex) {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloadFail"));
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        } finally {
            updateProgress(1, 1);
        }
        return false;
    }
}
