package mdgoassembler.view;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import mdgoassembler.MainApp;
import mdgoassembler.models.Assembly;
import mdgoassembler.models.Product;
import mdgoassembler.services.AssemblyService;
import mdgoassembler.services.ProductService;
import mdgoassembler.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static mdgoassembler.utils.Utils.*;

public class AssemblyViewController implements MsgBox {
    Logger LOGGER = LogManager.getLogger(this.getClass().getName());
    private Stage stage;
    private Stage statisticStage;
    private final AssemblyService assemblyService = new AssemblyService();
    private ObservableList<Assembly> allAssembly = FXCollections.observableArrayList();
    private ObservableList<Assembly> filteredAssembly = FXCollections.observableArrayList();
    private Method method;
    private MainApp mainApp;
    private int station;


    @FXML
    private DatePicker dateFrom;
    @FXML
    private DatePicker dateTo;
    @FXML
    private Button addBtn;
    @FXML
    private TextField filterField;
    @FXML
    private ImageView exportImg;
    @FXML
    private ImageView refreshImg;
    @FXML
    private ImageView statImg;
    @FXML
    private ChoiceBox<String> productBox;
    @FXML
    private TableView<Assembly> tAssembly;
    @FXML
    private CheckBox prntLabelCheck;
    @FXML
    private Label productBoxLbl;


    @FXML
    private void initialize() {
        statImg.setVisible(false);
        exportImg.setImage(new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("to_excel.png"))));
        refreshImg.setImage(new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("refresh.png"))));
        statImg.setImage(new Image(Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("statistic.png"))));

        productBox.setItems(FXCollections.observableArrayList(getProductNameList()));
        if (productBox.getItems().size() == 1) {
            productBox.getSelectionModel().selectFirst();
        }

        addBtn.setOnAction(e -> {
            switch (station) {
                case 1:
                    try {
                        addFirstAssembly();
                    } catch (CustomException ex) {
                        LOGGER.error("Exception", ex);
                        MsgBox.msgException(ex);
                    }
                    break;
                case 2:
                    addSecondAssembly();
                    break;
                default:
                    MsgBox.msgError("error");
            }
        });

        Tooltip.install(exportImg, new Tooltip("Export to Excel"));
        Tooltip.install(statImg, getStatTooltip());
        exportImg.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            stage.getScene().setCursor(Cursor.HAND);
            event.consume();
        });
        exportImg.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            stage.getScene().setCursor(Cursor.DEFAULT);
            event.consume();
        });
        Tooltip.install(refreshImg, new Tooltip("Refresh table"));
        refreshImg.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            stage.getScene().setCursor(Cursor.HAND);
            event.consume();
        });
        refreshImg.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            stage.getScene().setCursor(Cursor.DEFAULT);
            event.consume();
        });
        refreshImg.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            RotateTransition rt = new RotateTransition(Duration.millis(300), refreshImg);
            rt.setByAngle(360);
            rt.setCycleCount(1);
//            rt.setCycleCount(Animation.INDEFINITE);
            rt.setInterpolator(Interpolator.LINEAR);
            rt.play();
            event.consume();
        });
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) {
                filterTable(newValue.toUpperCase());
            } else {
                fillTable();
            }

        });

        prntLabelCheck.setDisable(!Utils.isPrintingAvailable());
//        statImg.setOnMouseEntered((event) -> {
//            test();
//        });
//        statImg.setOnMouseExited((event) -> {
//            statisticStage.close();
//        });

        initDate();
        fillTable();
    }

    @FXML
    private void setProductBox() {
        productBox.setItems(FXCollections.observableArrayList(getProductNameList()));
    }

    private TableRow<Assembly> rowFactoryTab(TableView<Assembly> view) {
        return new TableRow<>();
    }

    private void addTestColumnTab(String label, String dataIndex) {
        TableColumn<Assembly, String> column = new TableColumn<>(label);
        column.prefWidthProperty().bind(tAssembly.widthProperty().divide(10));
        column.setCellValueFactory(
                (TableColumn.CellDataFeatures<Assembly, String> param) -> {
                    ObservableValue<String> result = new ReadOnlyStringWrapper("");
                    if (param.getValue() != null) {
                        try {
                            method = param.getValue().getClass().getMethod(dataIndex);
                            result = new ReadOnlyStringWrapper("" + method.invoke(param.getValue()));
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            return result;
                        }
                    }
                    return result;
                }
        );
        column.setCellFactory(param -> new TableCell<Assembly, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                Assembly assembly = (Assembly) getTableRow().getItem();
                final ContextMenu menu = new ContextMenu();
                MenuItem mDeleteItem = new MenuItem("Delete item");
                mDeleteItem.setOnAction((e -> {
                    String q = String.format("Delete assembly SN: %s\nAre you sure?", assembly.getBoardSn());
                    if (MsgBox.msgConfirm(q)) {
                        deleteAssembly(assembly);
                    }
                }));
                menu.getItems().add(mDeleteItem);
                setContextMenu(menu);
                getContextMenu().setAutoHide(true);
                setText(item);
            }
        });
        tAssembly.getColumns().add(column);
    }

    private void addAssemblyColumnTab(String label, String dataIndex) {
        TableColumn<Assembly, String> column = new TableColumn<>(label);
        column.prefWidthProperty().bind(tAssembly.widthProperty().divide(4));
        column.setCellValueFactory(
                (TableColumn.CellDataFeatures<Assembly, String> param) -> {
                    ObservableValue<String> result = new ReadOnlyStringWrapper("");
                    if (param.getValue() != null) {
                        try {
                            method = param.getValue().getClass().getMethod(dataIndex);
                            result = new ReadOnlyStringWrapper("" + method.invoke(param.getValue()));
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            return result;
                        }
                    }
                    return result;
                }
        );
        column.setCellFactory(param -> new TableCell<Assembly, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                Assembly assembly = (Assembly) getTableRow().getItem();
                final ContextMenu menu = new ContextMenu();
                if (Utils.isPrintingAvailable()) {
                    MenuItem mPrintItem = new MenuItem("Print label");
                    mPrintItem.setOnAction((e -> printLabel(assembly)));
                    menu.getItems().add(mPrintItem);
                }

                MenuItem mDeleteItem = new MenuItem("Delete item");
                mDeleteItem.setOnAction((e -> {
                    String q = String.format("Delete assembly SN: %s\nAre you sure?", assembly.getBoardSn());
                    if (MsgBox.msgConfirm(q)) {
                        deleteAssembly(assembly);
                    }
                }));
                menu.getItems().add(mDeleteItem);

                setContextMenu(menu);
                getContextMenu().setAutoHide(true);
                setText(item);
            }
        });
        tAssembly.getColumns().add(column);
    }

    private void deleteAssembly(Assembly assembly) {
        String pass = MsgBox.msgInputPassword("Enter password for delete");
        if (pass != null) {
            if (pass.equals(String.format("%s%s", assembly.getSimPin(), Utils.getMinutesNow()))) {
                try {
                    assemblyService.deleteAssembly(assembly);
                    fillTable();
                    MsgBox.msgInfo("Delete complete");
                } catch (CustomException ex) {
                    MsgBox.msgWarning("Delete failure");
                }
            } else {
                MsgBox.msgWarning("Incorrect password");
            }
        }
    }

    @FXML
    private void fillTable() {
        tAssembly.getItems().clear();
        switch (station) {
            case 1:
                try {
                    allAssembly = FXCollections.observableArrayList(assemblyService.getIncompleteBetweenDate(
                            java.sql.Date.valueOf(dateFrom.getValue()),
                            java.sql.Date.valueOf(dateTo.getValue())));
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    allAssembly = FXCollections.observableArrayList(assemblyService.getCompleteBetweenDate(
                            java.sql.Date.valueOf(dateFrom.getValue()),
                            java.sql.Date.valueOf(dateTo.getValue())));
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                break;
            default:
        }
        tAssembly.setItems(allAssembly);
        fillStatistic();
    }

    private void filterTable(String val) {
        filteredAssembly.clear();
        for (Assembly assembly : allAssembly) {
            for (String s : assembly.getListForFilter()) {
                if (s.toUpperCase().contains(val)) {
                    filteredAssembly.add(assembly);
                    break;
                }
            }
        }
        tAssembly.setItems(filteredAssembly);
    }

    private void addFirstAssembly() throws CustomException {
        if (productBox.getValue() == null) {
            MsgBox.msgWarning("Product not selected");
            return;
        }
        Product product = new ProductService().findByName(productBox.getValue());

        String boardSn = checkInput(patternBoard, MsgBox.msgInputString("Board barcode:"));
        if (boardSn == null || isBoardPresent(boardSn)) {
            return;
        }
        String sim = checkInput(patternSim, MsgBox.msgInputString("SIM card barcode:"));
        if (sim == null) {
            return;
        }
        String simSn;
        String simPin;
        String[] simArr = sim.split("\\s");
        try {
            simSn = simArr[0];
            simPin = simArr[1];
            if (simSn == null || isSimPresent(simSn)) {
                return;
            }
        } catch (IndexOutOfBoundsException e) {
            MsgBox.msgWarning("Incorrect SIM card barcode");
            return;
        }

        String testQr = checkInput(patternQr, MsgBox.msgInputString("Test QR code:"));
        Map<String, String> qrMap = Utils.qrCsvToMap(testQr);
        if (qrMap != null) {
            qrMap.keySet().forEach(c -> {
                if (qrMap.get(c) == null) {
                    MsgBox.msgWarning("Incorrect test QR code");
                    return;
                }
            });
        } else {
            MsgBox.msgWarning("Incorrect test QR code");
            return;
        }
        try {
            Date burn = Utils.stringToDate(qrMap.get("Burn"));
            Date test1 = Utils.stringToDate(qrMap.get("Test1"));
            Date test2 = Utils.stringToDate(qrMap.get("Test2"));
            Assembly assembly = new Assembly(product, boardSn, simSn, simPin, qrMap.get("Id"), qrMap.get("FW"),
                    burn, test1, test2);
            assemblyService.saveAssembly(assembly);
            fillTable();
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgWarning(e.getLocalizedMessage());
        }
    }

    private void addSecondAssembly() {
        try {
            String boardSn = checkInput(patternBoard, MsgBox.msgInputString("Board barcode:"));
            if (boardSn == null || isBoardDone(boardSn)) {
                return;
            }
            Assembly assembly = assemblyService.findByBoard(boardSn);
            if (assembly == null) {
                MsgBox.msgWarning(String.format("Board with SN: %s not found", boardSn));
                return;
            }
            String caseSn = checkInput(patternCase, MsgBox.msgInputString("Case barcode:"));
            if (caseSn == null || isCasePresent(caseSn)) {
                return;
            }

            assembly.setAssemblyDate(new Date());
            assembly.setCaseSn(caseSn);
            assemblyService.updateAssembly(assembly);
            fillTable();
            if (prntLabelCheck.isSelected()) {
                printLabel(assembly);
            }
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgWarning(e.getLocalizedMessage());
        }

    }

    private List<String> getProductNameList() {
        List<String> productNameList = new ArrayList<>();
        try {
            ProductService productService = new ProductService();
            List<Product> productList = productService.findAll();
            productList.forEach(c -> productNameList.add(c.getName()));
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgError(e.getLocalizedMessage());
        }
        return productNameList;
    }

    private boolean isBoardPresent(String val) {
        try {
            boolean b = assemblyService.findByBoard(val) == null;
            if (b) {
                return false;
            } else {
                MsgBox.msgInfo(String.format("Board SN: %s already present", val));
                return true;
            }
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
        return false;
    }

    private boolean isBoardDone(String val) {
        try {
            Assembly assembly = assemblyService.findByBoard(val);
            if (assembly != null && assembly.getCaseSn() != null) {
                MsgBox.msgInfo(String.format("Board SN: %s already Done", val));
                return true;
            }
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
        return false;
    }

    private boolean isSimPresent(String val) {
        try {
            boolean b = assemblyService.findBySim(val) == null;
            if (b) {
                return false;
            } else {
                MsgBox.msgInfo(String.format("Sim card SN: %s already present", val));
                return true;
            }
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
        return false;
    }

    private boolean isCasePresent(String val) {
        try {
            boolean b = assemblyService.findByCase(val) == null;
            if (b) {
                return false;
            } else {
                MsgBox.msgInfo(String.format("Case SN: %s already present", val));
                return true;
            }
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
        return false;
    }

    private void fillStatistic() {
//        try {
//            toWorkLbl.setText(String.valueOf(assemblyService.getIncompleteCount()));
//            completeLbl.setText(String.valueOf(assemblyService.getCompleteCount()));
//            totalLbl.setText(String.valueOf(assemblyService.getTotalCount()));
//        } catch (CustomException e) {
//            LOGGER.error("Exception", e);
//            MsgBox.msgException(e);
//        }
    }

    @FXML
    private void generateReport() {
        ObservableList<Assembly> assemblyList = tAssembly.getItems();
        if (assemblyList.size() > 0) {
            ExcelReportGenerator repgen =
                    new ExcelReportGenerator(dateFrom.getValue(), dateTo.getValue(), tAssembly.getItems());
            repgen.assemblyReportToExcell();
        } else {
            MsgBox.msgInfo("There are no items for report");
        }

    }

    private void printLabel(Assembly assembly) {
        ZebraPrint zebraPrint = new ZebraPrint(assembly, Objects.requireNonNull(getSettings()).getPrnt_combo());
        zebraPrint.printLabel();
    }

    public void setStation(int station) {
        this.station = station;
        tAssembly.refresh();
        switch (station) {
            case 1:
                exportImg.setVisible(false);
                prntLabelCheck.setVisible(false);

                productBoxLbl.setVisible(true);
                productBox.setVisible(true);

                tAssembly.setRowFactory(this::rowFactoryTab);
                addTestColumnTab("Product PN", "getProductName");
                addTestColumnTab("Test date", "getStringTestDate");
                addTestColumnTab("Board SN", "getBoardSn");
                addTestColumnTab("SIM SN", "getSimSn");
                addTestColumnTab("SIM PIN", "getSimPin");
                addTestColumnTab("AWS ID", "getQrAwsId");
                addTestColumnTab("Firmware", "getQrFw");
                addTestColumnTab("Burn date", "getStringQrBurnDate");
                addTestColumnTab("Test1 date", "getStringQrTest1Date");
                addTestColumnTab("Test2 date", "getStringQrTest2Date");
                addTestColumnTab("Has history", "");
                fillTable();
                break;
            case 2:
                exportImg.setVisible(true);
                prntLabelCheck.setVisible(true);

                productBoxLbl.setVisible(false);
                productBox.setVisible(false);

                tAssembly.setRowFactory(this::rowFactoryTab);
                addAssemblyColumnTab("Product PN", "getProductName");
                addAssemblyColumnTab("Assembly date", "getStringAssemblyDate");
                addAssemblyColumnTab("Board SN", "getBoardSn");
                addAssemblyColumnTab("Case SN", "getCaseSn");
                fillTable();
                break;
            default:
                MsgBox.msgError("Table initialisation error");
        }
        tAssembly.setRowFactory(tv -> {
            TableRow<Assembly> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Assembly assembly = row.getItem();
                    if (mainApp.showAssemblyEditDialog(assembly)) {
                        fillTable();
                    }
                }
            });
            return row;
        });

    }

    private void initDate() {
        final String pattern = "yyyy-MM-dd";
        dateFrom.setShowWeekNumbers(true);
        dateFrom.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

            {
                dateFrom.setPromptText(pattern.toLowerCase());
            }

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        dateTo.setShowWeekNumbers(true);
        dateTo.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

            {
                dateTo.setPromptText(pattern.toLowerCase());
            }

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date input = cal.getTime();
        LocalDate curDate = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate start = curDate.withDayOfMonth(1);
        LocalDate stop = curDate.withDayOfMonth(curDate.lengthOfMonth());
        dateFrom.setValue(start);
        dateTo.setValue(stop);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    switch (station) {
                        case 1:
                            try {
                                addFirstAssembly();
                            } catch (CustomException e) {
                                LOGGER.error("Exception", e);
                                MsgBox.msgException(e);
                            }
                            break;
                        case 2:
                            addSecondAssembly();
                            break;
                    }
                }
            }
        });
    }


    private void test() {
        statisticStage = new Stage();
        statisticStage.initStyle(StageStyle.TRANSPARENT);
        statisticStage.setOpacity(.9);
        // Create the Button
        Button button = new Button("Hello");
        // Create the VBox
        VBox root = new VBox(button);
        // Create the Scene
        Scene scene = new Scene(root, 200, 100);
        // Add the Scene to the Stage
        statisticStage.setScene(scene);
        // Set the width and height of the Stage
        statisticStage.setWidth(400);
        statisticStage.setHeight(100);
        // Display the Stage
        statisticStage.show();
    }

    private Tooltip getStatTooltip() {
//        WebView wv = new WebView();
//        WebEngine we = wv.getEngine();
//        URL url = getClass().getClassLoader().getResource("StatTooltip.html");
//        System.out.println(url);
//        we.load(url.toString());

//        we.loadContent
//                (
//                        "<h3>Note:</h3> This is a Button"
//                );

        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-background-color: transparent;");
        tooltip.setPrefSize(300, 200);
        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

//        tooltip.setGraphic(wv);
        tooltip.setGraphic(new TableView<>());
        return tooltip;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
