package pg.gipter.statistic.dao;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import pg.gipter.dao.MongoDaoConfig;
import pg.gipter.statistic.dto.GipterUser;

class UserDaoImpl extends MongoDaoConfig implements UserDao {

    private MongoCollection<Document> usersCollection;

    UserDaoImpl() {
        super();
        usersCollection = database.getCollection(GipterUser.COLLECTION_NAME);
    }

    @Override
    public void updateUserStatistics(GipterUser user) {
        Document userToUpsert = Document.parse(new Gson().toJson(user, GipterUser.class));

        FindIterable<Document> users = usersCollection.find(Filters.eq("username", user.getUsername()));

        try (MongoCursor<Document> cursor = users.cursor()) {
            if (cursor.hasNext()) {
                userToUpsert = cursor.next();
                userToUpsert.put("lastExecutionDate", user.getLastExecutionDate());
                userToUpsert.remove("_id");
                usersCollection.updateOne(Filters.eq("username", user.getUsername()), userToUpsert);
            } else {
                usersCollection.insertOne(userToUpsert);
            }
        }

        logger.info("User statistics updated.");
    }
}
