package at.enactmentengine.serverless.slo;

import at.enactmentengine.serverless.slo.cost.CostHandler;
import java.io.IOException;
import java.sql.SQLException;

public final class SLOhandler {
    private static SLOhandler SLOhandler_instance = null;

    private DBhandler dbhandler;
    private MoDBhandler mdbhandler;
    private CostHandler costHandler;

    public void SLOhandler() {
    }

    public static SLOhandler getInstance() {
        if (SLOhandler_instance == null) {
            SLOhandler_instance = new SLOhandler();
        }
        return SLOhandler_instance;
    }

    public void init(String filenameSLOdb, String filenameMdb) throws IOException, SQLException {
        this.dbhandler = new DBhandler(filenameSLOdb);
        this.dbhandler.connectDB();

        this.mdbhandler = new MoDBhandler(filenameMdb);
        this.mdbhandler.init();

        this.costHandler = new CostHandler();
        this.costHandler.addEntries(dbhandler.getLambdaPricing());
        this.dbhandler.getSLOs();
        this.mdbhandler.testMongoDB();
    }

    public DBhandler getDbhandler() {
        return this.dbhandler;
    }

    public MoDBhandler getMDBhandler() {
        return this.mdbhandler;
    }

    public CostHandler getCostHandler(){ return this.costHandler; }

}