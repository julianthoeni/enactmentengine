package at.enactmentengine.serverless.slo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.*;

public class DBhandler {
    private String hostname;
    private String port;
    private String username;
    private String password;
    private Connection dbConnection;
    private Statement statement;

    private List<Integer> SLOs_id = new ArrayList<>();
    private List<String> SLOs_name = new ArrayList<>();
    private List<String> SLOs_unit = new ArrayList<>();

    public DBhandler(String fileName) throws IOException {

        Properties databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream(fileName));

        this.hostname = databaseProperties.getProperty("hostname");
        this.port = databaseProperties.getProperty("port");
        this.username = databaseProperties.getProperty("username");
        this.password = databaseProperties.getProperty("password");
        if (this.hostname == null || this.port == null || this.username == null || this.password == null)
            throw new IOException("sloDatabase.properties not correct");
    }


    public void connectDB() throws SQLException {
        try {
            System.out.println("Connecting to slo-database (" + this.hostname + ":" + this.port + ") ...");
            this.dbConnection = DriverManager.getConnection("jdbc:mariadb://" + this.hostname + "/", this.username, this.password);
            this.statement = this.dbConnection.createStatement();
            System.out.println("Successfully");
        } catch (SQLException e) {
            throw new SQLException("DB-Connection failed ...");
        }
    }

    public void closeDB() throws SQLException {
        this.dbConnection.close();
        System.out.println("Disconnected from slo-database");
    }

    public void getSLOs() throws SQLException {
        try {
            //TODO: Save the SLOs from the DB to the local SLO-Object
            String query = "SELECT * FROM afcl.slo";
            ResultSet result = this.statement.executeQuery(query);
            while (result.next()) {
                this.SLOs_id.add(Integer.valueOf(result.getString("sloid")));
                this.SLOs_name.add(result.getString("name"));
                this.SLOs_unit.add(result.getString("unit"));
            }
        } catch (SQLException e) {
            throw new SQLException("DB-Connection failed ...");
        }
    }

    public void printSLOs() {
        System.out.println(this.SLOs_id);
        System.out.println(this.SLOs_name);
        System.out.println(this.SLOs_unit);
    }
}
