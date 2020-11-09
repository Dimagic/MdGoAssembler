package mdgoassembler.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import mdgoassembler.MainApp;
import mdgoassembler.models.Product;
import mdgoassembler.services.ProductService;
import mdgoassembler.utils.CustomException;
import mdgoassembler.utils.MsgBox;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ProductsViewController {
    private final Logger LOGGER = LogManager.getLogger(this.getClass().getName());
    private ProductService productService = new ProductService();
    private MainApp mainApp;
    private Method method;

    @FXML
    private TableView<Product> tProducts;
    @FXML
    private Button deleteBtn;

    @FXML
    private void initialize() {
        tProducts.setRowFactory(this::rowFactoryTab);
        tProducts.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        deleteBtn.setDisable(true);
        addProductColumnTab("Product name", "getName");

        tProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteBtn.setDisable(newSelection == null);
        });

        deleteBtn.setOnAction(e -> {
            deleteProduct(tProducts.getSelectionModel().getSelectedItem());
        });

        fillTable();
    }

    @FXML
    private void addNewProduct(){
        try {
            String name = MsgBox.msgInputString("Enter name of product");
            if (name != null){
                Product product = new Product(name);
                productService.saveProduct(product);
                fillTable();
            }
        } catch (CustomException e){
            LOGGER.error("Exception", e);
            MsgBox.msgWarning(e.getLocalizedMessage());
        }

    }

    private void deleteProduct(Product product){
        try {
            productService.deleteProduct(product);
            fillTable();
        } catch (CustomException e) {
            e.printStackTrace();
        }

    }

    private void fillTable() {
        try {
            tProducts.getItems().clear();
            List productList = productService.findAll();
            tProducts.setItems(FXCollections.observableArrayList(productList));
            tProducts.refresh();
        } catch (CustomException e){
            LOGGER.error("Exception, e");
            MsgBox.msgException(e);
        }
    }

    private TableRow<Product> rowFactoryTab(TableView<Product> view) {
        return new TableRow<>();
    }

    private void addProductColumnTab(String label, String dataIndex) {
        TableColumn<Product, String> column = new TableColumn<>(label);
        column.prefWidthProperty().bind(tProducts.widthProperty().divide(1));
        column.setCellValueFactory(
                (TableColumn.CellDataFeatures<Product, String> param) -> {
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
        column.setCellFactory(param -> new TableCell<Product, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                Product product = (Product) getTableRow().getItem();
                setText(item);
            }
        });
        tProducts.getColumns().add(column);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
