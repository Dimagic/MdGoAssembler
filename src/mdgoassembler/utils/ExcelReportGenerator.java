package mdgoassembler.utils;

import javafx.collections.ObservableList;
import mdgoassembler.models.AssHistory;
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
import java.util.*;

public class ExcelReportGenerator {
    private static final Logger LOGGER = LogManager.getLogger(ExcelReportGenerator.class.getName());

    private final ObservableList<Assembly> reportData;
    private final String start;
    private final String stop;
    private XSSFWorkbook workbook;
    private Map<String, CellStyle> styles;
    private Cell cell;


    public ExcelReportGenerator(LocalDate start, LocalDate stop, ObservableList<Assembly> reportData) {
        this.reportData = reportData;
        this.start = Utils.localDateToString(start);
        this.stop = Utils.localDateToString(stop);
    }

    public boolean assemblyReportToExcell() {
        workbook = new XSSFWorkbook();
        String TITLE = "MdGo production report";
        String reportFolder = "./Reports/";
        String fileName = "MdGo_report.xlsx";
        XSSFSheet sheet = workbook.createSheet(TITLE);
        styles = createStyles(workbook);

        String[] titles = {"No", "Product P/N", "PCBA S/N", "SIM ICCID", "SIM PIN", "AWS ID", "Firmware",
                "Burn date", "Test1 date", "Test2 date","Modem", "Storage",
                "Base side sticker QR", "Assembly date", "History"};

        /*
          create the report head
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


        /*
          create the header row
         */
        int headerRowNum = 3;
        Row headerRow = sheet.createRow(headerRowNum);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < titles.length; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(styles.get("tableTitle"));
        }

        /*
          filling report data
         */
        int dataRowNum = headerRowNum;
        Row dataRow;
        for (Assembly assembly : reportData) {
            dataRowNum += 1;
            dataRow = sheet.createRow(dataRowNum);

            setDataCell(dataRow, Integer.toString(dataRowNum - headerRowNum));
            setDataCell(dataRow, assembly.getProduct().getName());
            setDataCell(dataRow, assembly.getBoardSn());
            setDataCell(dataRow, assembly.getSimSn());
            setDataCell(dataRow, assembly.getSimPin());
            setDataCell(dataRow, assembly.getQrAwsId());
            setDataCell(dataRow, assembly.getQrFw());
            setDataCell(dataRow, assembly.getStringQrBurnDate());
            setDataCell(dataRow, assembly.getStringQrTest1Date());
            setDataCell(dataRow, assembly.getStringQrTest2Date());
            setDataCell(dataRow, assembly.getQrModem());
            setDataCell(dataRow, assembly.getQrStorage());
            setDataCell(dataRow, assembly.getCaseSn());
            setDataCell(dataRow, assembly.getStringAssemblyDate());

            Set<AssHistory> historySet = assembly.getHistory();
            if (historySet.size() != 0){
                addHistorySheet(assembly.getBoardSn(), historySet);
                setHyperLink(dataRow, assembly.getBoardSn());
            } else {
                setDataCell(dataRow, "");
            }
        }

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

    private void setDataCell(Row dataRow, String value){
        int cellNum = dataRow.getFirstCellNum();
        if (cellNum == -1){
            cellNum = 0;
        } else {
            cellNum = dataRow.getLastCellNum();
        }
        cell = dataRow.createCell(cellNum);
        cell.setCellStyle(styles.get("dataCell_center"));
        cell.setCellValue(value);
    }

    private void setHyperLink(Row dataRow, String value){
        int cellNum = dataRow.getFirstCellNum();
        if (cellNum == -1){
            cellNum = 0;
        } else {
            cellNum = dataRow.getLastCellNum();
        }
        cell = dataRow.createCell(cellNum);
        cell.setCellValue("View");
        Hyperlink link = workbook.getCreationHelper().createHyperlink(Hyperlink.LINK_DOCUMENT);
        link.setAddress(String.format("'%s'!A1", value));
        cell.setHyperlink(link);
        cell.setCellStyle(styles.get("hyperlink"));
    }

    private void addHistorySheet(String boardSn, Set<AssHistory> historySet){
        XSSFSheet sheet = workbook.createSheet(boardSn);
        String TITLE = String.format("Board SN: %s history report", boardSn);
        String[] titles = {"Date", "Field", "Old value", "New value"};

        /*
          create the history head
         */
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, titles.length-1));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, titles.length-1));
        Row reportNameRow = sheet.createRow(0);
        cell = reportNameRow.createCell(0);
        cell.setCellValue(TITLE);
        cell.setCellStyle(styles.get("reportHeader"));

        /*
          create the header row
         */
        int headerRowNum = 2;
        Row headerRow = sheet.createRow(headerRowNum);
        headerRow.setHeightInPoints(12.75f);

        LinkedList<AssHistory> sortedHistory = new LinkedList<>(historySet);
        Collections.sort(sortedHistory);
        for (int i = 0; i < titles.length; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(styles.get("tableTitle"));
        }

        /*
          filling history data
         */
        int dataRowNum = headerRowNum;
        Row dataRow;
        for (AssHistory history : sortedHistory) {
            dataRowNum += 1;
            dataRow = sheet.createRow(dataRowNum);

            setDataCell(dataRow, Utils.getFormattedDate(history.getDate()));
            setDataCell(dataRow, history.getFieldChange());
            setDataCell(dataRow, history.getOldValue());
            setDataCell(dataRow, history.getNewValue());
        }

        for (int j = 0; j < titles.length; j++) {
            sheet.autoSizeColumn(j);
        }
    }

    /*
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

        font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setColor(IndexedColors.BLUE.getIndex());
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font);
        styles.put("hyperlink", style);

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
}

