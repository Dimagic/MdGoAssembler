package mdgoassembler.utils;

import javafx.collections.ObservableList;
import mdgoassembler.models.Assembly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ExcelReportGenerator {
    private static final Logger LOGGER = LogManager.getLogger(ExcelReportGenerator.class.getName());

    private final ObservableList<Assembly> reportData;
    private final String start;
    private final String stop;
    private Map<String, CellStyle> styles;
    private Cell cell;


    public ExcelReportGenerator(LocalDate start, LocalDate stop, ObservableList<Assembly> reportData) {
        this.reportData = reportData;
        this.start = Utils.localDateToString(start);
        this.stop = Utils.localDateToString(stop);
    }

    public boolean assemblyReportToExcell() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        String TITLE = "MdGo production report";
        String reportFolder = "./Reports/";
        String fileName = "MdGo_report.xlsx";
        XSSFSheet sheet = workbook.createSheet(TITLE);
        styles = createStyles(workbook);

        String[] titles = {"No", "Product P/N", "PCBA S/N", "SIM ICCID", "SIM PIN", "AWS ID", "Firmware",
                "Burn date", "Test1 date", "Test2 date","Base side sticker QR", "Assembly date"};

        /**
         * create the report head
         */
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length-1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, titles.length-1));
        Row reportNameRow = sheet.createRow(0);
        cell = reportNameRow.createCell(0);
        cell.setCellValue(TITLE);
        cell.setCellStyle(styles.get("reportHeader"));
        Row reportPeriodRow = sheet.createRow(1);
        cell = reportPeriodRow.createCell(0);
        cell.setCellValue(String.format("from %s to %s", start, stop));
        cell.setCellStyle(styles.get("reportHeader"));


        /**
         * create the header row
         */
        int headerRowNum = 3;
        Row headerRow = sheet.createRow(headerRowNum);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < titles.length; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(styles.get("tableTitle"));
        }

        /**
         * filling report data
         */
        int dataRowNum = headerRowNum;
        Row dataRow;
        String currArticle;
        Map<String, Integer> summaryData = new HashMap<String, Integer>();
        for (Assembly assembly : reportData) {
            dataRowNum += 1;
            dataRow = sheet.createRow(dataRowNum);

            setDataCell(dataRow, "dataCell_center", Integer.toString(dataRowNum - headerRowNum));
            setDataCell(dataRow, "dataCell_center", assembly.getProduct().getName());
            setDataCell(dataRow, "dataCell_center", assembly.getBoardSn());
            setDataCell(dataRow, "dataCell_center", assembly.getSimSn());
            setDataCell(dataRow, "dataCell_center", assembly.getSimPin());
            setDataCell(dataRow, "dataCell_center", assembly.getQrAwsId());
            setDataCell(dataRow, "dataCell_center", assembly.getQrFw());
            setDataCell(dataRow, "dataCell_center", assembly.getStringQrBurnDate());
            setDataCell(dataRow, "dataCell_center", assembly.getStringQrTest1Date());
            setDataCell(dataRow, "dataCell_center", assembly.getStringQrTest2Date());
            setDataCell(dataRow, "dataCell_center", assembly.getCaseSn());
            setDataCell(dataRow, "dataCell_center", assembly.getStringAssemblyDate());
        }

        /**
         * filling report statistic by article
         */
//        int lastRow = sheet.getLastRowNum() + 2;
//        dataRow = sheet.createRow(lastRow);
//        setDataCell(dataRow, "tableTitle", "Systems statistic");
//        setDataCell(dataRow, "tableTitle", "");
//        sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow, 0, 1));
//        dataRow = sheet.createRow(lastRow + 1);
//        setDataCell(dataRow, "tableTitle", "Article");
//        setDataCell(dataRow, "tableTitle", "Count");
//
//        dataRowNum = sheet.getLastRowNum();
//        int total = 0;
//        List<String> keyList = new ArrayList<String>(summaryData.keySet());
//        Collections.sort(keyList);
//        for (String key: keyList){
//            dataRowNum += 1;
//            dataRow = sheet.createRow(dataRowNum);
//            setDataCell(dataRow, "dataCell_center", key);
//            setDataCell(dataRow, "dataCell_center", Integer.toString(summaryData.get(key)));
//            total = total + summaryData.get(key);
//        }
//        dataRow = sheet.createRow(dataRowNum + 1);
//        setDataCell(dataRow, "dataCell_right_bold", "Total:");
//        setDataCell(dataRow, "tableTitle", Integer.toString(total));

        for (int j = 0; j < titles.length; j++) {
            sheet.autoSizeColumn(j);
        }

        File f = new File(reportFolder);
        if (!f.exists()){
            if (!f.mkdirs()){
                MsgBox.msgError("MdGo-assembler", String.format("Cant't create folder: %s", reportFolder));
                return false;
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(String.format("%s%s", reportFolder, fileName))) {
            workbook.write(outputStream);
            Desktop.getDesktop().open(new File(String.format("%s%s", reportFolder, fileName)));
        } catch (IOException e) {
            MsgBox.msgWarning(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void setDataCell(Row dataRow, String style, String value){
        int cellNum = dataRow.getFirstCellNum();
        if (cellNum == -1){
            cellNum = 0;
        } else {
            cellNum = dataRow.getLastCellNum();
        }
        cell = dataRow.createCell(cellNum);
        cell.setCellStyle(styles.get(style));
        cell.setCellValue(value);
    }

    /**
     * create a library of cell styles
     */
    private Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<>();
        CellStyle style;
        Font font;

        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font);
        styles.put("reportHeader", style);

        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(font);
        styles.put("tableTitle", style);

        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font);
        styles.put("dataCell_center", style);

        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(font);
        styles.put("dataCell_right_bold", style);

        return styles;
    }

    private CellStyle createBorderedStyle(Workbook wb){
        short thin = 1;
        short black = IndexedColors.BLACK.getIndex();

        CellStyle style = wb.createCellStyle();
        style.setBorderRight(thin);
        style.setRightBorderColor(black);
        style.setBorderBottom(thin);
        style.setBottomBorderColor(black);
        style.setBorderLeft(thin);
        style.setLeftBorderColor(black);
        style.setBorderTop(thin);
        style.setTopBorderColor(black);
        return style;
    }

    public boolean isOfficeInstalled(){
        try {
            Process p = Runtime.getRuntime().exec
                    (new String [] { "cmd.exe", "/c", "assoc", ".xls"});
            BufferedReader input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            String extensionType = input.readLine();
            input.close();
            // extract type
            if (extensionType == null) {
                return false;
            }
            String fileType[] = extensionType.split("=");

            p = Runtime.getRuntime().exec
                    (new String [] { "cmd.exe", "/c", "ftype", fileType[1]});
            input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            String fileAssociation = input.readLine();
            // extract path
//            String officePath = fileAssociation.split("=")[1];
            return true;
        }
        catch (Exception e) {
            LOGGER.error(e);
            MsgBox.msgException(e);
        }
        return false;
    }

}

