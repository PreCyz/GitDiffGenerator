package pg.gipter.dao;

import com.mongodb.MongoClient;
import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDao {

    private String host;//mongodb+srv://gipter:retpig@gipter-test-ruzxs.mongodb.net/test?retryWrites=true&w=majority
    private String username;
    private String password;
    private String databaseName;//gipter
    private MongoClient mongoClient;

    public void init() {
        MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED);
        String uri = String.format("mongodb+srv://%s:%s@%s", username, password, host);
        MongoClientURI mongoClientURI = new MongoClientURI(uri, mongoClientOptionsBuilder);
        this.mongoClient = new MongoClient(mongoClientURI);
        databaseName = "gipter";
    }

    void tryTest() {
        MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();

        for(String db : databaseNames){
            System.out.println(db);
        }
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> users = database.getCollection("users");

        users.insertOne(Document.parse("{\"userName\":\"pawg\"}"));

        BasicDBObject query = new BasicDBObject();
        query.put("userName", new BasicDBObject("$eq", "pawg"));
        FindIterable<Document> documents = users.find(query);

        MongoCursor<Document> cursor = documents.cursor();
        while(cursor.hasNext()) {
            System.out.println(cursor.next());
        }
    }
}
