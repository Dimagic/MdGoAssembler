package mdgoassembler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mdgoassembler.models.Assembly;
import mdgoassembler.utils.HibernateSessionFactoryUtil;
import mdgoassembler.utils.MsgBox;
import mdgoassembler.utils.Settings;
import mdgoassembler.utils.Utils;
import mdgoassembler.view.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainApp extends Application {
    Logger LOGGER = LogManager.getLogger(MainApp.class.getName());
    private final String VERSION = "0.0.3.9";
    private final Image favicon = new Image(Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream("logo.png")));
    private RootLayoutController rootLayoutController;
    private Stage primaryStage;
    private BorderPane rootLayout;
    private Settings currentSettings;
    private final List<String> stations = new ArrayList<>();
    private final String appName = "MdGo-assembler";
    private final String appNameWithVer = String.format("%s v%s", appName, VERSION);


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(appNameWithVer);
        this.primaryStage.getIcons().add(favicon);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("RootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            rootLayoutController = loader.getController();
            rootLayoutController.setMainApp(this);
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        stations.add("First station");
        stations.add("Second station");
        if (loadSettings()) {
            checkDbConnection();
        } else {
            MsgBox.msgWarning("Validation settings file is fail.\n" +
                    "Please check settings and try again.");
        }
    }

    private void showAssemblyView(int station) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AssemblyView.fxml"));
            AnchorPane assemblyPage = loader.load();
            rootLayout.setCenter(assemblyPage);
            AssemblyViewController assemblyViewController = loader.getController();
            assemblyViewController.setMainApp(this);
            assemblyViewController.setStage(primaryStage);
            assemblyViewController.setStation(station);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showProductsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ProductsView.fxml"));
            AnchorPane page = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Products");
            stage.getIcons().add(favicon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.setResizable(false);
            ProductsViewController productsViewController = loader.getController();
            productsViewController.setMainApp(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSettingsDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("SettingsView.fxml"));
            AnchorPane page = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.getIcons().add(favicon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.setResizable(false);
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setMainApp(this);
            settingsViewController.fillSettings();
            settingsViewController.setDialogStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean showAssemblyEditDialog(Assembly assembly) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("AssemblyEditView.fxml"));
            AnchorPane page = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Edit assembly");
            stage.getIcons().add(favicon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.setResizable(false);
            AssemblyEditViewController controller = loader.getController();
            controller.setStage(stage);
            controller.setMainApp(this);
            controller.setAssembly(assembly);
            stage.showAndWait();
            return controller.isSaved();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void showAssemblyHistoryView(Assembly assembly, Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("AssemblyHistoryView.fxml"));
            AnchorPane page = loader.load();

            Stage stage = new Stage();
            stage.setTitle(String.format("History of board: %s", assembly.getBoardSn()));
            stage.getIcons().add(favicon);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            Scene scene = new Scene(page);
            stage.setScene(scene);
            stage.setResizable(false);
            AssemblyHistoryViewController controller = loader.getController();
            controller.setHistorySet(assembly.getHistory());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadSettings() {
        Settings settings = Utils.getSettings();
        if (settings != null && settings.validate()){
            setCurrentSettings(settings);
            return settings.validate();
        }
        return false;
    }

    private void checkDbConnection() {
        try {
            Map<String, String> connectionInfo = HibernateSessionFactoryUtil.getConnectionInfo();
            if (connectionInfo != null) {
                rootLayoutController.setCurrentDbLbl(connectionInfo.get("DataBaseUrl"));
                rootLayoutController.setSwitchMenuEnable(true);
                changeStation();
            } else {
                rootLayoutController.setSwitchMenuEnable(false);
                rootLayoutController.setCurrentDbLbl("No DB connection");
            }
        } catch (Exception e) {
            LOGGER.error("Exception", e);
            MsgBox.msgException(e);
        }
    }

    public void changeStation() {
        String choice = MsgBox.msgChoice("Please select station", "", stations);
        if (choice != null) {
            switch (choice) {
                case "First station":
                    showAssemblyView(1);
                    break;
                case "Second station":
                    showAssemblyView(2);
                    break;
            }
            primaryStage.setTitle(String.format("%s: %s", appNameWithVer, choice));
        }
    }

    public void setCurrentSettings(Settings currentSettings) {
        this.currentSettings = currentSettings;
    }

    public Settings getCurrentSettings() {
        return currentSettings;
    }

    public static void main(String[] args) {
        launch(args);
    }


}
