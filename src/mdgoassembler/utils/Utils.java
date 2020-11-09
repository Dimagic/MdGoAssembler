package mdgoassembler.utils;

import mdgoassembler.MainApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils implements MsgBox{
    private static Logger LOGGER = LogManager.getLogger(MainApp.class.getName());
    public static Pattern patternBoard = Pattern.compile("^[A-Z]{4}[\\d]{11}$");
    public static Pattern patternSim = Pattern.compile("[\\d]{19}[\\s][\\d]{6}");
    public static Pattern patternQr = Pattern.compile("^(?:(?<=\")([^\"]*)(?=\"))|(?<=,|^)([^,]*)(?=,|$)");
    public static Pattern patternCase = Pattern.compile("^[\\d]{15}$");
    public static Pattern patternTemplate = Pattern.compile("^(\\^XA)(\\W|\\w)*(<SN>)(\\W|\\w)*(<SN>)(\\W|\\w)*(\\^XZ)$");

    public static Settings getSettings() {
        try {
            File file = new File("./settings.xml");
            if (!file.exists()) {
                MsgBox.msgWarning("Load settings", "Settings file not found.\n" +
                        "Will create a new settings file.\nPlease fill settings form.");
                if (file.createNewFile()){
                    return getSettings();
                }
            } else if(file.length() != 0) {
                JAXBContext context = JAXBContext.newInstance(Settings.class);
                Unmarshaller um = context.createUnmarshaller();
                return (Settings) um.unmarshal(file);
            }
        } catch (Exception e) {
            LOGGER.error("Exceprion", e);
            MsgBox.msgException(e);
        }
        return null;
    }

    public static String getFormattedDate(Date date){
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dt.format(date);
    }

    public static String localDateToString(LocalDate date){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return date.format(fmt);
    }

    public static Date stringToDate(String val){
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String s = val.replaceAll("[a-zA-Z]", " ");
            Date date = dt.parse(s);
            return date;
        } catch (ParseException e) {
            LOGGER.error("Exceprion", e);
            MsgBox.msgException(e);
        }
        return null;
    }

    public static String checkInput(Pattern p, String val){
        Matcher matcher;
        try {
            String s = val.trim();
            matcher = p.matcher(s);
            if (!matcher.find()) {
                MsgBox.msgWarning("Incorrect input");
                return null;
            }
        } catch (NullPointerException e){
            return null;
        }
        return val;
    }

    public static int getMinutesNow(){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        return  calendar.get(Calendar.MINUTE);
    }

    public static Map<String, String> qrCsvToMap(String csvString) {
        String line = "";
        String cvsSplitBy = ",";
        Map<String, String> importingData = new HashMap<>();
        Reader inputString = new StringReader(csvString);
        BufferedReader br = new BufferedReader(inputString);
        try {
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(cvsSplitBy);
                for(String s: tmp){
                    String key = s.split(":\\s")[0];
                    String val = s.split(":\\s")[1];
                    importingData.put(key, val);
                }
            }
            return importingData;
        } catch (Exception e) {
            LOGGER.error("Exceprion", e);
            MsgBox.msgException(e);
        }
        return null;
    }

    public static boolean isPrintingAvailable(){
        return Objects.requireNonNull(getSettings()).getPrnt_combo() != null &&
                checkTemplate(getSettings().getTemplate_area());
    }

    public static boolean checkTemplate(String template){
        Matcher mTemplate = patternTemplate.matcher(template.trim());
        return mTemplate.find();
    }
}
