package mdgoassembler.view;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import mdgoassembler.models.AssHistory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Set;

public class AssemblyHistoryViewController {
    Logger LOGGER = LogManager.getLogger(this.getClass().getName());
    private Method method;
    private Set<AssHistory> historySet;

    @FXML
    private TableView<AssHistory> tHistory;

    @FXML
    private void initialize() {
        tHistory.setRowFactory(this::rowFactoryTab);
        addHistoryColumnTab("Date", "getStringDate");
        addHistoryColumnTab("Field", "getFieldChange");
        addHistoryColumnTab("Old value", "getOldValue");
        addHistoryColumnTab("New value", "getNewValue");
    }

    private TableRow<AssHistory> rowFactoryTab(TableView<AssHistory> view) {
        return new TableRow<>();
    }

    private void addHistoryColumnTab(String label, String dataIndex) {
        TableColumn<AssHistory, String> column = new TableColumn<>(label);
        column.prefWidthProperty().bind(tHistory.widthProperty().divide(4));
        column.setCellValueFactory(
                (TableColumn.CellDataFeatures<AssHistory, String> param) -> {
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
        column.setCellFactory(param -> new TableCell<AssHistory, String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                AssHistory assHistory = (AssHistory) getTableRow().getItem();
                setText(item);
            }
        });
        tHistory.getColumns().add(column);
    }

    private void fillTable() {
        ObservableList<AssHistory> res = FXCollections.observableArrayList(historySet);;
        FXCollections.sort(res, Comparator.comparingLong(AssHistory::getDateMilliseconds));
        tHistory.setItems(FXCollections.observableArrayList(res));
    }

    public void setHistorySet(Set<AssHistory> historySet) {
        this.historySet = historySet;
        fillTable();
    }
}
