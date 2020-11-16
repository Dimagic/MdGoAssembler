package mdgoassembler.models;

import javafx.beans.property.SimpleStringProperty;
import mdgoassembler.utils.Utils;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "AssHistory")
public class AssHistory implements Comparable<AssHistory>{

    public AssHistory() {
    }

    public AssHistory(Assembly assembly, String fieldChange, String oldValue, String newValue) {
        setDate(new Date());
        this.assembly = assembly;
        this.fieldChange = fieldChange;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assembly_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assembly assembly;

    public Assembly getAssembly() {
        return assembly;
    }

    public void setAssembly(Assembly assembly) {
        this.assembly = assembly;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStringDate(){
        return Utils.getFormattedDate(getDate());
    }

    @Column(nullable = false, length = 128)
    private String fieldChange;

    public String getFieldChange() {
        return fieldChange;
    }

    public void setFieldChange(String fieldChange) {
        this.fieldChange = fieldChange;
    }

    @Column(nullable = false, length = 128)
    private String oldValue;

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @Column(nullable = false, length = 128)
    private String newValue;

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public SimpleStringProperty fieldProperty(){
        return new SimpleStringProperty(getFieldChange());
    }

    public SimpleStringProperty valueProperty(){
        return new SimpleStringProperty(getOldValue());
    }

    public SimpleStringProperty dateProperty(){
        return new SimpleStringProperty(dateToString(getDate()));
    }

    private String dateToString(Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public Long getDateMilliseconds(){
        return getDate().getTime();
    }

    @Override
    public String toString() {
        return "AssHistory{" +
                "id=" + id +
                ", assembly=" + assembly +
                ", date=" + date +
                ", fieldChange='" + fieldChange + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }

    @Override
    public int compareTo(AssHistory assHistory) {
        return date.compareTo(assHistory.getDate());
    }
}
