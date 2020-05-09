package pg.gipter.statistics.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pg.gipter.core.dao.MongoDaoConfig;
import pg.gipter.core.producer.command.VersionControlSystem;
import pg.gipter.statistics.Statistic;
import pg.gipter.utils.StringUtils;

import java.util.*;

class StatisticDaoImpl extends MongoDaoConfig implements StatisticDao {

    StatisticDaoImpl() {
        super(Statistic.COLLECTION_NAME);
    }

    @Override
    public void updateStatistics(Statistic statistic) {
        FindIterable<Document> users = collection.find(Filters.eq("username", statistic.getUsername()));

        try (MongoCursor<Document> cursor = users.cursor()) {
            Document userToUpsert = Document.parse(new Gson().toJson(statistic, Statistic.class));
            if (cursor.hasNext()) {
                userToUpsert = cursor.next();
                userToUpsert.put("lastExecutionDate", statistic.getLastExecutionDate());
                userToUpsert.put("javaVersion", statistic.getJavaVersion());
                userToUpsert.put("lastUpdateStatus", statistic.getLastUpdateStatus().name());
                userToUpsert.put("lastRunType", statistic.getLastRunType().name());

                List<String> existingSystemUsers = Optional.ofNullable(userToUpsert.getList("systemUsers", String.class))
                        .orElseGet(ArrayList::new);
                LinkedHashSet<String> systemUsers = new LinkedHashSet<>(existingSystemUsers);
                systemUsers.addAll(statistic.getSystemUsers());
                userToUpsert.put("systemUsers", systemUsers);

                Map<String, Set<String>> controlSystemMap = Optional.ofNullable(userToUpsert.get("controlSystemMap", Map.class))
                        .orElseGet(LinkedHashMap::new);
                for (Map.Entry<VersionControlSystem, Set<String>> entry : statistic.getControlSystemMap().entrySet()) {
                    if (controlSystemMap.containsKey(entry.getKey().name())) {
                        Set<String> scvs = new LinkedHashSet<>(controlSystemMap.get(entry.getKey().name()));
                        scvs.addAll(entry.getValue());
                        controlSystemMap.put(entry.getKey().name(), scvs);
                    } else {
                        controlSystemMap.put(entry.getKey().name(), entry.getValue());
                    }
                }
                userToUpsert.put("controlSystemMap", controlSystemMap);

                if (!StringUtils.nullOrEmpty(statistic.getLastSuccessDate())) {
                    userToUpsert.put("lastSuccessDate", statistic.getLastSuccessDate());
                }
                if (!StringUtils.nullOrEmpty(statistic.getLastFailedDate())) {
                    userToUpsert.put("lastFailedDate", statistic.getLastFailedDate());
                }

                collection.updateOne(
                        Filters.eq(userToUpsert.getObjectId("_id")),
                        new BasicDBObject().append("$set", userToUpsert)
                );
            } else {
                collection.insertOne(userToUpsert);
            }
            logger.info("User statistics updated.");
        } catch (Exception ex) {
            logger.error("Could not update statistics.", ex);
        }
    }
}
