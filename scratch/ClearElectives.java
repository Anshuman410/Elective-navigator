import com.mongodb.client.*;
import org.bson.Document;

public class ClearElectives {
    public static void main(String[] args) {
        String uri = "mongodb+srv://sakacoders:saka123@electivenavigator.ognfxkl.mongodb.net/?appName=ElectiveNavigator";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("electivenavigator");
            MongoCollection<Document> collection = database.getCollection("electives");
            
            long count = collection.countDocuments();
            System.out.println("Deleting " + count + " documents from 'electives' collection...");
            
            collection.deleteMany(new Document());
            
            System.out.println("Collection cleared successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
