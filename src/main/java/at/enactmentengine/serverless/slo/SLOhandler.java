package at.enactmentengine.serverless.slo;

import java.io.IOException;
import java.sql.SQLException;

public final class SLOhandler {
    private static SLOhandler SLOhandler_instance = null;

    private DBhandler dbhandler;
    private MoDBhandler mdbhandler;

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

        this.dbhandler.getSLOs();
        this.mdbhandler.testMongoDB();
        //System.out.println(this.mdbhandler.getFunctionAvgRTTinPeriod("arn:aws:lambda:us-east-1:468730259750:function:xwf01_convertValues", 30000000L));
        //System.out.println(this.mdbhandler.getFunctionSuccessRateInPeriod("arn:aws:lambda:us-east-1:468730259750:function:xwf01_convertValues", 30000000L));
    }

    public DBhandler getDbhandler() {
        return this.dbhandler;
    }

    public MoDBhandler getMDBhandler() {
        return this.mdbhandler;
    }

}