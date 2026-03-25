import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    private static final String URI = "mongodb+srv://sakacoders:saka123@electivenavigator.ognfxkl.mongodb.net/?appName=ElectiveNavigator";
    private static final String DB_NAME = "electivenavigator";

    public static MongoDatabase getDatabase() {

        MongoClient client = MongoClients.create(URI);
        return client.getDatabase(DB_NAME);

    }
}