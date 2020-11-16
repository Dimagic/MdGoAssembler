package mdgoassembler.view;


import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mdgoassembler.MainApp;
import mdgoassembler.utils.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsViewController implements MsgBox {
	Logger LOGGER = LogManager.getLogger(this.getClass().getName());
	@FXML
	private TitledPane db_pane;
	@FXML
	private TitledPane printer_pane;
	@FXML
	private TextField addr_db;
	@FXML
	private TextField port_db;
	@FXML
	private TextField name_db;
	@FXML
	private TextField user_db;
	@FXML
	private PasswordField pass_db;
	@FXML
	private ComboBox<String> prnt_combo;
	@FXML
	private TextArea template_area;

	private MainApp mainApp;
	private Stage dialogStage;
	private Settings currSettings;
	
	public SettingsViewController(){

	}
	
	@FXML
    private void initialize(){
		db_pane.setCollapsible(false);
		printer_pane.setCollapsible(false);
		fillPrintersBox();

	}

    @FXML
    private void cancelBtnClick(){
    	dialogStage.close();
    }
    
    @FXML
    private void saveBtnClick(){
    	if (saveSettings()){
//			mainApp.setDbUrl();
    		mainApp.loadSettings();
    	}
    }
    
    @FXML
    private void testDBconn(){
		try {
			if (saveSettings()) {
//				mainApp.loadSettings();
				Session session = HibernateUtils.getSession();
				if (session != null){
					session.close();
				}
				MsgBox.msgInfo("DB connection", "Connection established");
			}
		} catch (Exception | ExceptionInInitializerError | NoClassDefFoundError e) {
			LOGGER.error("Exception", e);
			MsgBox.msgError("DB connection fail.\n" +
					"See log file for details.");
		}

    }


    public void fillSettings() {
    	try {
    		Settings currSet = Utils.getSettings();
    		addr_db.setText(currSet.getAddr_db());
    		port_db.setText(currSet.getPort_db());
    		name_db.setText(currSet.getName_db());
    		user_db.setText(currSet.getUser_db());
    		pass_db.setText(currSet.getPass_db());
			prnt_combo.setValue(currSet.getPrnt_combo());
			String template = currSet.getTemplate_area().trim();
			if (!template.isEmpty()){
				if (Utils.checkTemplate(template)){
					template_area.setText(currSet.getTemplate_area());
				} else {
					MsgBox.msgWarning("Can't load incorrect template");
				}
			}
    	} catch (NullPointerException e) {
			return;
		}
    }

	public Settings getCurrSettings() {
		return currSettings;
	}

	private void setCurrSettings(Settings currSettings) {
		this.currSettings = currSettings;
	}

	private boolean saveSettings() {
		HashMap<String, String> sett = new HashMap<>();
    	Field[] fields = this.getClass().getDeclaredFields();
		for(Field field: fields) {
			try {
				if (field.getType() == TextField.class) {
					TextField textField = (TextField) field.get(this);
					sett.put(field.getName(), textField.getText());
				}
				else if (field.getType() == PasswordField.class) {
					System.out.println(field.getName() + " >>> " + field.toString());
					PasswordField passwordField = (PasswordField) field.get(this);
					sett.put(field.getName(), passwordField.getText());
				}
				else if (field.getType() == ComboBox.class) {
					System.out.println(field.getName() + " >>> " + field.toString());
					ComboBox<String> comboField = (ComboBox<String>) field.get(this);
					sett.put(field.getName(), comboField.getValue());
				}
				else if (field.getType() == TextArea.class){
					TextArea areaField = (TextArea) field.get(this);
					String template = areaField.getText().trim();
					if (!template.isEmpty() && !Utils.checkTemplate(template)){
						MsgBox.msgWarning("Incorrect template");
						return false;
					}
					sett.put(field.getName(), areaField.getText());

				}

//				String zplString = template_area.getText().replace("<SN>", "1234567890");
//				File outputfile = new File("image.jpg");
//				try {
//					ImageIO.write(generateQRCodeImage(zplString), "jpg", outputfile);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}

				/**
				 * Write settings to the currentSettings in MainApp.
				 */
				setCurrSettings(new Settings(sett));
				mainApp.setCurrentSettings(getSettings());
			} catch (NullPointerException e) {
				MsgBox.msgError(String.format("Field %s not found", field.getName()));
				continue;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				MsgBox.msgException(e);
			}
		}
		if (!getCurrSettings().validate()){
			MsgBox.msgWarning("Not all fields are filled");
			return false;
		}

		File file = new File("./settings.xml");
	    try {
	    	System.out.println(sett);
	        JAXBContext context = JAXBContext
	                .newInstance(Settings.class);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        Settings wrapper = new Settings(sett);
	        System.out.println(wrapper);
	        m.marshal(wrapper, file);
	        MsgBox.msgInfo("Save settings", "Save settings successfully complete.");
//			dialogStage.close();
	        return true;
	    } catch (Exception e) { 
	    	MsgBox.msgException(e);
	        return false;
	    }
	}

	private void fillPrintersBox(){
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		List<String> prntList = new ArrayList<>();
		for (PrintService printer : printServices){
			prntList.add(printer.getName());
		}
		prnt_combo.setItems(FXCollections.observableArrayList(prntList));
	}
	
	public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

    }

	public Settings getSettings(){
		return mainApp.getCurrentSettings();
	}


	public void setDialogStage(Stage dialogStage) {
    	this.dialogStage = dialogStage;
    }
}
