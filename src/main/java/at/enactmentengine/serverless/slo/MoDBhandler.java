package at.enactmentengine.serverless.slo;

import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import static com.mongodb.client.model.Filters.*;

public class MoDBhandler {
    private String hostname;
    private String port;
    private String username;
    private String password;
    private String database;
    private String collection;

    private MongoClient client;
    private MongoDatabase mongodatabase;
    private MongoCollection<Document> mongoCollection;

    public MoDBhandler(String fileName) throws IOException {

        Properties databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream(fileName));

        this.hostname = databaseProperties.getProperty("host");
        this.port = databaseProperties.getProperty("port");
        this.username = databaseProperties.getProperty("username");
        this.password = databaseProperties.getProperty("password");
        this.database = databaseProperties.getProperty("database");
        this.collection = databaseProperties.getProperty("collection");

        if (this.hostname == null || this.port == null || this.username == null || this.password == null || this.database == null || this.collection == null)
            throw new IOException("mongoDatabase.properties not correct");
    }

    public void init() {
        this.client = MongoClients.create("mongodb://" + this.hostname + ":" + this.port);
        this.mongodatabase = client.getDatabase(this.database);
        this.mongoCollection = this.mongodatabase.getCollection(this.collection);
    }

    public void testMongoDB() {
        Bson equalComparison = lte("RTT", 1115);
        int counter = 0;
        for (Document doc : this.mongoCollection.find(equalComparison)) {
            counter++;
        }
    }

    public int getFunctionAvgRTTinPeriod(String arn, long period){
        Bson equalComparison = lte("function_id", arn);
        int i = 0;
        long sum = 0;

        for (Document doc : this.mongoCollection.find(equalComparison)) {
            Date refDate = new Date();
            refDate.setTime(refDate.getTime() - period);
            if(((Date) doc.get("startTime")).after(refDate)){
                sum += (Long) doc.get("RTT");
                i++;
            }
        }
        if(i==0) return 0;
        return (int) sum/i;
    }

    public double getFunctionSuccessRateInPeriod(String arn, long period){
        int i = 0;
        int failures = 0;
        Bson equalComparison = lte("function_id", arn);
        for (Document doc : this.mongoCollection.find(equalComparison)) {
            Date refDate = new Date();
            refDate.setTime(refDate.getTime() - period);
            boolean isSuccess = (boolean) doc.get("success");
            if(((Date) doc.get("startTime")).after(refDate)) {
                if (!isSuccess) failures++;
                i++;
            }
        }
        if(i==0) return 0;
        return (double) failures/i;
    }

    public double getFunctionTotalCostInPeriod(String arn, long period){
        //Todo: Implement function callculation for a function
        return 0;
    }
}