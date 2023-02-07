package at.enactmentengine.serverless.slo;

import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import static com.mongodb.client.model.Sorts.descending;

import static com.mongodb.client.model.Filters.*;

public class MoDBhandler {
    private static final Logger logger = LoggerFactory.getLogger(Rule.class);
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
        try{
            this.client = MongoClients.create("mongodb://" + this.hostname + ":" + this.port);
            this.mongodatabase = client.getDatabase(this.database);
            this.mongoCollection = this.mongodatabase.getCollection(this.collection);
            logger.info("Connected to MongoDB");
        }catch (Exception e){
            logger.warn("Connection to MongoDB (" + this.hostname + ":" + this.port + ") failed");
            throw e;
        }
    }

    public void addEntriesToRule(String functionName, Rule rule) throws ParseException {
        Bson equalComparison = eq("functionName", functionName);
        for (Document doc : this.mongoCollection.find(equalComparison)) {
            SimpleDateFormat dateFormater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
            long date = dateFormater.parse(doc.get("startTime").toString()).getTime();
            rule.addDataEntry((long) doc.get("RTT"),date,(double) doc.get("cost"),(boolean) doc.get("success"),doc.get("function_id").toString());
        }
    }

    public void close(){
        client.close();
        logger.info("Disconnected from MongoDB");
    }

    public double getPreviousPrice(String arn){
        Bson equalComparison = lte("function_id", arn);
        Document doc = this.mongoCollection.find(equalComparison).sort(descending("startTime")).first();

        double prev_price = 0d;
        if(doc != null) {
            prev_price = (double) doc.get("cost");
        }
        return prev_price;
    }

}