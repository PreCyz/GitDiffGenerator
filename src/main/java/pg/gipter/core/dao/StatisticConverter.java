package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.statistics.ExceptionDetails;
import pg.gipter.statistics.Statistic;
import pg.gipter.ui.RunType;
import pg.gipter.ui.UploadStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class StatisticConverter {

    public Document convert(Statistic statistic) {
        Document document = new Document();
        document.put("_id", statistic.getId());
        document.put("applicationVersion", statistic.getApplicationVersion());
        document.put("firstExecutionDate", statistic.getFirstExecutionDate());
        document.put("javaVersion", statistic.getJavaVersion());
        document.put("lastExecutionDate", statistic.getLastExecutionDate());
        document.put("lastFailedDate", statistic.getLastFailedDate());
        document.put("lastRunType", statistic.getLastRunType().name());
        document.put("lastUpdateStatus", statistic.getLastUpdateStatus().name());
        document.put("lastSuccessDate", statistic.getLastSuccessDate());
        document.put("username", statistic.getUsername());
        document.put("systemUsers", statistic.getSystemUsers());
        final Map<String, Set<String>> controlSystemMap = statistic.getControlSystemMap()
                .entrySet()
                .stream()
                .collect(toMap(entry -> entry.getKey().name(), Map.Entry::getValue, (v1, v2) -> v1));
        document.put("controlSystemMap", controlSystemMap);

        final ExceptionDetailsConverter converter = new ExceptionDetailsConverter();
        final List<Document> exceptions = statistic.getExceptions().stream().map(converter::convert).collect(toList());
        document.put("exceptions", exceptions);
        return document;
    }

    public Statistic convert(Document document) {
        Statistic statistic = new Statistic();
        statistic.setId(document.getObjectId("_id"));
        statistic.setApplicationVersion(document.getString("applicationVersion"));
        statistic.setFirstExecutionDate(document.getString("firstExecutionDate"));
        statistic.setJavaVersion(document.getString("javaVersion"));
        statistic.setLastExecutionDate(document.getString("lastExecutionDate"));
        statistic.setLastFailedDate(document.getString("lastFailedDate"));
        statistic.setLastRunType(RunType.valueOf(document.getString("lastRunType")));
        statistic.setLastUpdateStatus(UploadStatus.valueOf(document.getString("lastUpdateStatus")));
        statistic.setLastSuccessDate(document.getString("lastSuccessDate"));
        statistic.setUsername(document.getString("username"));
        statistic.setSystemUsers(new LinkedHashSet<>(document.getList("systemUsers", String.class)));
        statistic.setExceptions(getExceptions(document));
        statistic.setControlSystemMap(getVersionControlSystemSetMap(document));

        return statistic;
    }

    private List<ExceptionDetails> getExceptions(Document document) {
        List<Document> exceptions = Optional.ofNullable(document.getList("exceptions", Document.class))
                .orElseGet(ArrayList::new);
        return exceptions.stream()
                    .map(d -> new ExceptionDetails(d.getString("errorMsg"), d.getString("cause"), d.getString("errorDate")))
                    .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private Map<VersionControlSystem, Set<String>> getVersionControlSystemSetMap(Document document) {
        Map<String, Set<String>> controlSystemMap = Optional.ofNullable(document.get("controlSystemMap", Map.class))
                .orElseGet(HashMap::new);
        return controlSystemMap.entrySet()
                .stream()
                .collect(toMap(
                        entry -> VersionControlSystem.valueFor(entry.getKey()),
                        Map.Entry::getValue,
                        (v1, v2) -> v1));
    }

}
