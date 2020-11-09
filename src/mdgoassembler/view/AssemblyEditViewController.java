package mdgoassembler.view;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mdgoassembler.MainApp;
import mdgoassembler.models.AssHistory;
import mdgoassembler.models.Assembly;
import mdgoassembler.models.Product;
import mdgoassembler.services.AssHistoryService;
import mdgoassembler.services.AssemblyService;
import mdgoassembler.services.ProductService;
import mdgoassembler.utils.CustomException;
import mdgoassembler.utils.MsgBox;
import mdgoassembler.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

import static mdgoassembler.utils.Utils.*;

public class AssemblyEditViewController {
    private final Logger LOGGER = LogManager.getLogger(this.getClass().getName());

    private Assembly assembly;
    private MainApp mainApp;
    private Stage stage;
    private boolean isChanged = false;
    private boolean isSaved = false;
    private final AssemblyService assemblyService = new AssemblyService();
    private final Map<String, AssHistory> historyMap = new HashMap<>();
    private Map<String, String > fieldNames;

    @FXML
    private GridPane gridPane;
    @FXML
    private ComboBox<String> productBox;
    @FXML
    private MenuItem saveMenu;
    @FXML
    private Button historyBtn;
    @FXML
    private TextField boardSn;
    @FXML
    private TextField simSn;
    @FXML
    private TextField simPin;
    @FXML
    private TextField awsId;
    @FXML
    private TextField fw;
    @FXML
    private TextField burnDate;
    @FXML
    private TextField test1Date;
    @FXML
    private TextField test2Date;
    @FXML
    private TextField caseSn;
    @FXML
    private TextField assemblyDate;

    @FXML
    private void initialize() {
        saveMenu.setDisable(true);
        fieldNames = getFieldName();
    }

    @FXML
    public void showHistory(){
        mainApp.showAssemblyHistoryView(assembly, stage);
    }

    public void setAssembly(Assembly assembly) {
        this.assembly = assembly;
        productBox.setItems(FXCollections.observableArrayList(getProductNameList()));
        productBox.getSelectionModel().select(assembly.getProduct().getName());
        boardSn.setText(assembly.getBoardSn());
        simSn.setText(assembly.getSimSn());
        simPin.setText(assembly.getSimPin());
        awsId.setText(assembly.getQrAwsId());
        fw.setText(assembly.getQrFw());
        burnDate.setText(Utils.getFormattedDate(assembly.getQrBurnDate()));
        test1Date.setText(Utils.getFormattedDate(assembly.getQrTest1Date()));
        test2Date.setText(Utils.getFormattedDate(assembly.getQrTest2Date()));
        caseSn.setText(assembly.getCaseSn());
        try {
            assemblyDate.setText(Utils.getFormattedDate(assembly.getAssemblyDate()));
        } catch (NullPointerException ignored){}
        historyBtn.setVisible(assembly.getHistory().size() != 0);
        initChangeListeners();
    }

    @FXML
    private void changeSim() {
        String sim = checkInput(patternSim, MsgBox.msgInputString("SIM card barcode:"));
        if (sim == null) {
            return;
        }
        String sn, pin;
        String[] simArr = sim.split("\\s");
        try {
            sn = simArr[0];
            pin = simArr[1];
            if (sn == null || isSimPresent(sn)) {
                return;
            }
            if (!simSn.getText().equalsIgnoreCase(sn)) {
                simSn.setText(sn);
            }
            if (!simPin.getText().equalsIgnoreCase(pin)) {
                simPin.setText(pin);
            }
        } catch (IndexOutOfBoundsException e) {
            MsgBox.msgWarning("Incorrect SIM card barcode");
            return;
        }
    }

    @FXML
    private void changeTest() {
        String testQr = checkInput(patternQr, MsgBox.msgInputString("Test QR code:"));
        if (testQr == null){
            return;
        }
        Map<String, String> qrMap = Utils.qrCsvToMap(testQr);
        // Checking if was scan correct QR code, where 5 is numbers of items
        if (qrMap != null && qrMap.size() == 5) {
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
        awsId.setText(qrMap.get("Id"));
        fw.setText(qrMap.get("FW"));
        burnDate.setText(Utils.getFormattedDate(stringToDate(qrMap.get("Burn"))));
        test1Date.setText(Utils.getFormattedDate(stringToDate(qrMap.get("Test1"))));
        test2Date.setText(Utils.getFormattedDate(stringToDate(qrMap.get("Test2"))));
    }

    @FXML
    private void changeCase() {
        String caseSerial = checkInput(patternCase, MsgBox.msgInputString("Case barcode:"));
        if (caseSerial == null || isCasePresent(caseSerial)){
            return;
        }
        caseSn.setText(caseSerial);
        assemblyDate.setText(Utils.getFormattedDate(new Date()));
    }

    @FXML
    private void saveAssembly(){
        try {
            assembly.setProduct(new ProductService().findByName(productBox.getSelectionModel().getSelectedItem()));
            assembly.setBoardSn(boardSn.getText());
            assembly.setSimSn(simSn.getText());
            assembly.setSimPin(simPin.getText());
            assembly.setQrAwsId(awsId.getText());
            assembly.setQrFw(fw.getText());
            assembly.setQrBurnDate(Utils.stringToDate(burnDate.getText()));
            assembly.setQrTest1Date(Utils.stringToDate(test1Date.getText()));
            assembly.setQrTest2Date(Utils.stringToDate(test2Date.getText()));
            assembly.setCaseSn(caseSn.getText());
            assembly.setAssemblyDate(!assemblyDate.getText().equals("") ?
                    Utils.stringToDate(assemblyDate.getText()): null);
            AssHistoryService assHistoryService = new AssHistoryService();
            if (assHistoryService.saveList(new ArrayList<>(historyMap.values()))){
                Set<AssHistory> historySet = new HashSet<>(assHistoryService.findAssHistoryByAssembly(assembly));
                assembly.setHistory(historySet);
                assemblyService.updateAssembly(assembly);
                isSaved = true;
                stage.close();
            }
        } catch (CustomException e){
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
    }

    private void initChangeListeners() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getType() == javafx.scene.control.TextField.class) {
                    TextField currField = (TextField) field.get(this);
                    currField.textProperty().addListener((observable, oldValue, newValue) -> {
                        currField.setStyle("-fx-background-color: yellow;");
                        historyMap.put(field.getName(), new AssHistory(assembly, fieldNames.get(field.getName()), oldValue, newValue));
                        isChanged = true;
                        saveMenu.setDisable(false);
                    });
                } else if (field.getType() == javafx.scene.control.ComboBox.class) {
                    ComboBox<String> currField = (ComboBox<String>) field.get(this);
                    currField.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
                        historyMap.put(field.getName(), new AssHistory(assembly, fieldNames.get(field.getName()),
                                oldValue, newValue));
                        isChanged = true;
                        saveMenu.setDisable(false);
                    });
                }
            } catch (NullPointerException e) {
                MsgBox.msgError("Field %s not found\n", field.getName());
                continue;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("Exception", e);
                MsgBox.msgException(e);
            }
        }
    }

    private boolean isSimPresent(String val) {
        try {
            Assembly assembly = assemblyService.findBySim(val);
            if (assembly != null && !assembly.getBoardSn().equalsIgnoreCase(boardSn.getText())) {
                String msg = String.format("SIM card No: %s\n" +
                        "already use with board No: %s", val, assembly.getBoardSn());
                MsgBox.msgInfo(msg);
                return true;
            }
            return false;
        } catch (CustomException e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
        return false;
    }

    private boolean isCasePresent(String val){
        try {
            boolean b = assemblyService.findByCase(val) == null;
            if (b){
                return false;
            } else {
                MsgBox.msgInfo(String.format("Case SN: %s already present", val));
                return true;
            }
        } catch (CustomException e) {
            LOGGER.error(e);
            MsgBox.msgException(e);
        }
        return false;
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

    private Map<String, String> getFieldName() {
        Map<String, String> gridMap = new HashMap<>();
        List<Node> nodeList = gridPane.getChildren();
        int colCount = gridPane.getColumnConstraints().size();
        int row;
        for (Node node : nodeList) {
            row = node.getProperties().get("gridpane-row") == null ? 0 :
                    (Integer) node.getProperties().get("gridpane-row");
            if (node.getClass() == Label.class){
                String name = ((Label) node).getText().replace(":", "");
                String s = nodeList.get(nodeList.size()/colCount + row).getId();
                gridMap.put(s, name);
            }
        }
        return gridMap;
    }

    public boolean isSaved(){
        return isSaved;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::confirmCloseEvent);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    private <T extends Event> void confirmCloseEvent(T t) {
        if (isChanged && !isSaved){
            if (!MsgBox.msgConfirm("Assembly data was changed.\n" +
                    "Do you want exit without saving?")){
                t.consume();
            }

        }
    }
}
