package at.enactmentengine.serverless.slo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class SLO_LOGGER {

    private static SLO_LOGGER INSTANCE;
    private long startTime;

    public SLO_LOGGER(){
        this.startTime = System.currentTimeMillis();
    }

    public static SLO_LOGGER getINSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new SLO_LOGGER();
        }
        return INSTANCE;
    }

    public String getTime(){
        return String.valueOf(System.currentTimeMillis() - this.startTime);
    }


    public void writeToLog(String msg){
        try {
            File file = new File("log/logging.txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(getTime().concat(" - ").concat(msg.concat("\n")));

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeRTT(String function_name, String msg){
        try {
            File file = new File("log/logging_rtt_" +function_name + ".txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(getTime().concat(" - ").concat(msg.concat("\n")));

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCost(String function_name, String msg){
        try {
            File file = new File("log/logging_cost_" +function_name + ".txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(getTime().concat(" - ").concat(msg.concat("\n")));

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFailR(String function_name, String msg){
        try {
            File file = new File("log/logging_failr_" +function_name + ".txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(getTime().concat(" - ").concat(msg.concat("\n")));

            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
