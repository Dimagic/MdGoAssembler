package mdgoassembler.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import mdgoassembler.MainApp;

public class RootLayoutController {
    private MainApp mainApp;

    @FXML
    private Label currentDbLbl;

    @FXML
    private MenuItem switchMenu;

    @FXML
    public void openSettings(){
        mainApp.showSettingsDialog();
    }

    @FXML
    public void showProducts(){
        mainApp.showProductsView();
    }

    @FXML
    public void changeStation(){
        mainApp.changeStation();
    }

    @FXML
    private void exit(){
        System.exit(0);
    }

    public void setSwitchMenuEnable(boolean val){
        switchMenu.setDisable(!val);
    }

    public void setCurrentDbLbl(String val){
        currentDbLbl.setText(val);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
