package tutorial;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;

public class QuickStart {

    private static Dotenv dotenv = Dotenv.configure().directory("./").load();
    private static String uri = dotenv.get("MONGOURI");

    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("tBot");
            MongoCollection<Document> collection = database.getCollection("matches");

            Document doc = collection.find(eq("name", "test")).first();
            if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("No matching documents found.");
            }
        }
    }

    public static void registerMatch(Match match, int alertType) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase tBotDB = mongoClient.getDatabase("tBot");
            MongoCollection<Document> matchesCollection = tBotDB.getCollection("matches");

            Document matchDoc = new Document("_id", new ObjectId());
            matchDoc
                .append("eventId", match.eventId)
                .append("alertType", alertType)
                .append("minutes", match.minutes)
                .append("country", match.country)
                .append("tournament", match.tournament)
                .append("homeTeam", match.homeTeam)
                .append("awayTeam", match.awayTeam)
                .append("homeScore", match.homeScore)
                .append("awayScore", match.awayScore)
                .append("homeCorners", match.homeCorners)
                .append("awayCorners", match.awayCorners)
                .append("homeShots", match.homeShots)
                .append("awayShots", match.awayShots)
                .append("homeShotsOn", match.homeShotsOn)
                .append("awayShotsOn", match.awayShotsOn)
                .append("homeShotsOff", match.homeShotsOff)
                .append("awayShotsOff", match.awayShotsOff)
                .append("homeShotsBlocked", match.homeShotsBlocked)
                .append("awayShotsBlocked", match.awayShotsBlocked)
                .append("homePossession", match.homePossession)
                .append("awayPossession", match.awayPossession)
                .append("homeOdds", match.homeOdds)
                .append("drawOdds", match.drawOdds)
                .append("awayOdds", match.awayOdds)
                .append("cornerLine", match.cornerLine)
                .append("result", null);

            matchesCollection.insertOne(matchDoc);
        }
    }
}
