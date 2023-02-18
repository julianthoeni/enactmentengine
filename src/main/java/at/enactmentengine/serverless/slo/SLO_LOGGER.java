package at.enactmentengine.serverless.slo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class SLO_LOGGER {
    private static final Logger logger = LoggerFactory.getLogger(Rule.class);
    private static SLO_LOGGER INSTANCE;
    private static long startTime;
    private boolean enabled = false;

    public SLO_LOGGER(){

    }

    public static SLO_LOGGER getINSTANCE(){
        if(INSTANCE == null){
            startTime = System.currentTimeMillis();
            INSTANCE = new SLO_LOGGER();
        }
        return INSTANCE;
    }

    public void setEnabled(boolean isEnabled){
        this.enabled = isEnabled;
        if(isEnabled){
            logger.info("Logging slo-infos in ENABLED");
        }else{
            logger.info("Logging slo-infos is DISABLED");
        }
    }

    public String getTime(){
        return String.valueOf(System.currentTimeMillis());
    }


    public void writeToLog(String msg){
        if(this.enabled){
            try {
                File file = new File("log/logging.txt");
                FileWriter fr = new FileWriter(file, true);
                BufferedWriter br = new BufferedWriter(fr);
                br.write(getTime().concat(",").concat(msg.concat("\n")));

                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeRTT(String function_name, String msg, String resourceLink){
        if(this.enabled){
            try {
                File file = new File("log/logging_rtt_" +function_name + ".txt");
                FileWriter fr = new FileWriter(file, true);
                BufferedWriter br = new BufferedWriter(fr);
                br.write(getTime().concat(",").concat(resourceLink).concat(",").concat(msg.concat("\n")));

                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeCost(String function_name, String msg, String resourceLink){
        if(this.enabled){
            try {
                File file = new File("log/logging_cost_" +function_name + ".txt");
                FileWriter fr = new FileWriter(file, true);
                BufferedWriter br = new BufferedWriter(fr);
                br.write(getTime().concat(",").concat(resourceLink).concat(",").concat(msg.concat("\n")));

                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeFailR(String function_name, String msg, String resourceLink){
        if(this.enabled){
            try {
                File file = new File("log/logging_failr_" +function_name + ".txt");
                FileWriter fr = new FileWriter(file, true);
                BufferedWriter br = new BufferedWriter(fr);
                br.write(getTime().concat(",").concat(resourceLink).concat(",").concat(msg.concat("\n")));

                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
