package mdgoassembler.utils;

import mdgoassembler.view.SettingsViewController;

import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.util.HashMap;

@XmlRootElement(name = "settings")
public class Settings extends SettingsViewController implements MsgBox {
	private String addr_db;
	private String port_db;
	private String name_db;
	private String user_db;
	private String pass_db;
	private String prnt_combo;
	private String template_area;
	
	public Settings(){	
	}
	
	public Settings(HashMap<String, String> args) {
		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for(Field field: fields) {
				field.setAccessible(true);
				field.set(this, args.get(field.getName()));
			}
		} catch (SecurityException
                | IllegalArgumentException 
                | IllegalAccessException e) {
            MsgBox.msgException(e);
		}
	}

	public boolean validate(){
		return !addr_db.isEmpty() && !port_db.isEmpty() && !name_db.isEmpty() && !user_db.isEmpty() && !pass_db.isEmpty();
	}
	
	public String getAddr_db() {
		return addr_db;
	}

	public void setAddr_db(String addr_db) {
		this.addr_db = addr_db;
	}

	public String getPort_db() {
		return port_db;
	}

	public void setPort_db(String port_db) {
		this.port_db = port_db;
	}

	public String getName_db() {
		return name_db;
	}

	public void setName_db(String name_db) {
		this.name_db = name_db;
	}

	public String getUser_db() {
		return user_db;
	}

	public void setUser_db(String user_db) {
		this.user_db = user_db;
	}

	public String getPass_db() {
		return pass_db;
	}

	public void setPass_db(String pass_db) {
		this.pass_db = pass_db;
	}

	public String getPrnt_combo() {
		return prnt_combo;
	}

	public void setPrnt_combo(String prnt_combo) {
		this.prnt_combo = prnt_combo;
	}

	public String getTemplate_area() {
		return template_area;
	}

	public void setTemplate_area(String template_area) {
		this.template_area = template_area;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"addr_db='" + addr_db + '\'' +
				", port_db='" + port_db + '\'' +
				", name_db='" + name_db + '\'' +
				", user_db='" + user_db + '\'' +
				", pass_db='" + pass_db + '\'' +
				", prnt_combo='" + prnt_combo + '\'' +
				", template_area='" + template_area + '\'' +
				'}';
	}
}
