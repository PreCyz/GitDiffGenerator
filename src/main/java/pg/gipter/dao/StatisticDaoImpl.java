package pg.gipter.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import pg.gipter.statistic.dto.Statistics;

class StatisticDaoImpl extends MongoDaoConfig implements StatisticDao {

    StatisticDaoImpl() {
        super(Statistics.COLLECTION_NAME);
    }

    @Override
    public void updateStatistics(Statistics statistics) {
        Bson searchQuery = Filters.eq("username", statistics.getUsername());

        Document userToUpsert = Document.parse(new Gson().toJson(statistics, Statistics.class));

        FindIterable<Document> users = collection.find(searchQuery);
        try (MongoCursor<Document> cursor = users.cursor()) {
            if (cursor.hasNext()) {
                userToUpsert = cursor.next();
                userToUpsert.put("lastExecutionDate", statistics.getLastExecutionDate());
                userToUpsert.put("javaVersion", statistics.getJavaVersion());
                userToUpsert.put("lastUpdateStatus", statistics.getLastUpdateStatus().name());
                userToUpsert.put("lastRunType", statistics.getLastRunType().name());
                searchQuery = Filters.eq(userToUpsert.getObjectId("_id"));

                collection.updateOne(searchQuery, new BasicDBObject().append("$set", userToUpsert));
            } else {
                collection.insertOne(userToUpsert);
            }
            logger.info("User statistics updated.");
        } catch (Exception ex) {
            logger.error("Could not update statistics.", ex);
        }
    }
}
