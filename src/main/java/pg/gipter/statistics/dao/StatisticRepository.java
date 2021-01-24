package pg.gipter.statistics.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pg.gipter.core.dao.MongoDaoConfig;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.statistics.ExceptionDetails;
import pg.gipter.statistics.Statistic;
import pg.gipter.utils.StringUtils;

import java.util.*;

class StatisticRepository extends MongoDaoConfig implements StatisticDao {

    StatisticRepository() {
        super(Statistic.COLLECTION_NAME);
    }

    @Override
    public void updateStatistics(Statistic statistic) {
        FindIterable<Statistic> users = collection.find(
                Filters.eq("username", statistic.getUsername()),
                Statistic.class
        );

        try (MongoCursor<Statistic> cursor = users.cursor()) {
            Statistic statisticToUpsert;
            if (cursor.hasNext()) {
                statisticToUpsert = cursor.next();
                statisticToUpsert.setLastExecutionDate(statistic.getLastExecutionDate());
                statisticToUpsert.setJavaVersion(statistic.getJavaVersion());
                statisticToUpsert.setLastUpdateStatus(statistic.getLastUpdateStatus());
                statisticToUpsert.setLastRunType(statistic.getLastRunType());
                statisticToUpsert.setApplicationVersion(statistic.getApplicationVersion());

                LinkedHashSet<String> systemUsers = new LinkedHashSet<>(
                        Optional.ofNullable(statisticToUpsert.getSystemUsers()).orElseGet(LinkedHashSet::new)
                );
                systemUsers.addAll(statistic.getSystemUsers());
                statisticToUpsert.setSystemUsers(systemUsers);

                Map<VersionControlSystem, Set<String>> controlSystemMap =
                        Optional.ofNullable(statisticToUpsert.getControlSystemMap()).orElseGet(LinkedHashMap::new);
                for (Map.Entry<VersionControlSystem, Set<String>> entry : statistic.getControlSystemMap().entrySet()) {
                    if (controlSystemMap.containsKey(entry.getKey())) {
                        Set<String> scvs = new LinkedHashSet<>(controlSystemMap.get(entry.getKey()));
                        scvs.addAll(entry.getValue());
                        controlSystemMap.put(entry.getKey(), scvs);
                    } else {
                        controlSystemMap.put(entry.getKey(), entry.getValue());
                    }
                }
                statisticToUpsert.setControlSystemMap(controlSystemMap);

                if (!StringUtils.nullOrEmpty(statistic.getLastSuccessDate())) {
                    statisticToUpsert.setLastSuccessDate(statistic.getLastSuccessDate());
                }
                if (!StringUtils.nullOrEmpty(statistic.getLastFailedDate())) {
                    statisticToUpsert.setLastFailedDate(statistic.getLastFailedDate());
                }

                List<ExceptionDetails> exceptions = new LinkedList<>(
                        Optional.ofNullable(statisticToUpsert.getExceptions()).orElseGet(LinkedList::new)
                );
                exceptions.addAll(statistic.getExceptions());
                statisticToUpsert.setExceptions(exceptions);

                collection.updateOne(
                        Filters.eq(statisticToUpsert.getId()),
                        new BasicDBObject().append("$set", statisticToUpsert)
                );
            } else {
                Document document = Document.parse(new Gson().toJson(statistic, Statistic.class));
                collection.insertOne(document);
            }
            logger.info("User statistics updated.");
        } catch (Exception ex) {
            logger.error("Could not update statistics.", ex);
        }
    }
}
