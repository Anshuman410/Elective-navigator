import com.mongodb.client.*;
import org.bson.Document;
import java.util.Arrays;
import java.util.List;

public class ClearDatabase {
    public static void main(String[] args) {
        String uri = "mongodb+srv://sakacoders:saka123@electivenavigator.ognfxkl.mongodb.net/?appName=ElectiveNavigator";
        List<String> collectionsToClear = Arrays.asList("electives", "students", "queries", "feedback");
        
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("electivenavigator");
            
            for (String collName : collectionsToClear) {
                MongoCollection<Document> collection = database.getCollection(collName);
                long count = collection.countDocuments();
                System.out.println("Deleting " + count + " documents from '" + collName + "' collection...");
                collection.deleteMany(new Document());
                System.out.println("'" + collName + "' cleared.");
            }
            
            System.out.println("\nDatabase reset complete. Total fresh start initialized.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
