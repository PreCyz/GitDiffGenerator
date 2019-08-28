package pg.gipter.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pg.gipter.statistic.dto.Statistics;
import pg.gipter.utils.StringUtils;

class StatisticDaoImpl extends MongoDaoConfig implements StatisticDao {

    StatisticDaoImpl() {
        super(Statistics.COLLECTION_NAME);
    }

    @Override
    public void updateStatistics(Statistics statistics) {
        FindIterable<Document> users = collection.find(Filters.eq("username", statistics.getUsername()));

        try (MongoCursor<Document> cursor = users.cursor()) {
            Document userToUpsert = Document.parse(new Gson().toJson(statistics, Statistics.class));
            if (cursor.hasNext()) {
                userToUpsert = cursor.next();
                userToUpsert.put("lastExecutionDate", statistics.getLastExecutionDate());
                userToUpsert.put("javaVersion", statistics.getJavaVersion());
                userToUpsert.put("lastUpdateStatus", statistics.getLastUpdateStatus().name());
                userToUpsert.put("lastRunType", statistics.getLastRunType().name());
                if (!StringUtils.nullOrEmpty(statistics.getLastSuccessDate())) {
                    userToUpsert.put("lastSuccessDate", statistics.getLastSuccessDate());
                }
                if (!StringUtils.nullOrEmpty(statistics.getLastFailedDate())) {
                    userToUpsert.put("lastFailedDate", statistics.getLastFailedDate());
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
