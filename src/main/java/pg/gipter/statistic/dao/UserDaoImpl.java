package pg.gipter.statistic.dao;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import pg.gipter.dao.MongoDaoConfig;
import pg.gipter.statistic.dto.Statistics;

class UserDaoImpl extends MongoDaoConfig implements UserDao {

    private MongoCollection<Document> usersCollection;

    UserDaoImpl() {
        super();
        usersCollection = database.getCollection(Statistics.COLLECTION_NAME);
    }

    @Override
    public void updateUserStatistics(Statistics user) {
        Bson searchQuery = Filters.eq("username", user.getUsername());

        Document userToUpsert = Document.parse(new Gson().toJson(user, Statistics.class));

        FindIterable<Document> users = usersCollection.find(searchQuery);
        try (MongoCursor<Document> cursor = users.cursor()) {
            if (cursor.hasNext()) {
                userToUpsert = cursor.next();
                userToUpsert.put("lastExecutionDate", user.getLastExecutionDate());
                userToUpsert.put("javaVersion", user.getJavaVersion());
                userToUpsert.put("lastUpdateStatus", user.getLastUpdateStatus().name());
                userToUpsert.put("lastRunType", user.getLastRunType().name());
                searchQuery = Filters.eq(userToUpsert.getObjectId("_id"));

                usersCollection.updateOne(searchQuery, new BasicDBObject().append("$set", userToUpsert));
            } else {
                usersCollection.insertOne(userToUpsert);
            }
            logger.info("User statistics updated.");
        } catch (Exception ex) {
            logger.error("Could not update statistics.", ex);
        }
    }
}
