package mdgoassembler.models;

import mdgoassembler.utils.Utils;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "Assembly", uniqueConstraints = { @UniqueConstraint(columnNames = "boardSn"),
        @UniqueConstraint(columnNames = "simSn") })
public class Assembly {

    public Assembly() {
    }

    public Assembly(Product product, String boardSn, String simSn, String simPin, String qrId, String qrFw,
                    Date qrBurnDate, Date qrTest1Date, Date qrTest2Date) {
        this.testDate = new Date();
        this.product = product;
        this.boardSn = boardSn;
        this.simSn = simSn;
        this.simPin = simPin;
        this.qrAwsId = qrId;
        this.qrFw = qrFw;
        this.qrBurnDate = qrBurnDate;
        this.qrTest1Date = qrTest1Date;
        this.qrTest2Date = qrTest2Date;
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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "testdate")
    private Date testDate;

    public Date getTestDate() {
        return testDate;
    }

//    @Column(name = "productpn", length = 15, nullable = false)
    @ManyToOne(cascade=CascadeType.ALL)
    private Product product;

    public Product getProduct() {
        return product;
    }

    public String getProductName(){
        return getProduct().getName();
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Column(name = "boardsn", length = 15, nullable = false, unique = true)
    private String boardSn;

    public String getBoardSn() {
        return boardSn;
    }

    public void setBoardSn(String boardSn) {
        this.boardSn = boardSn;
    }

    @Column(name = "simsn", length = 26, nullable = false, unique = true)
    private String simSn;

    public String getSimSn() {
        return simSn;
    }

    public void setSimSn(String simSn) {
        this.simSn = simSn;
    }

    @Column(name = "simpin", length = 6, nullable = false)
    private String simPin;

    public String getSimPin() {
        return simPin;
    }

    public void setSimPin(String simPin) {
        this.simPin = simPin;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assemblydate")
    private Date assemblyDate;

    public Date getAssemblyDate() {
        return assemblyDate;
    }

    public void setAssemblyDate(Date assemblyDate) {
        this.assemblyDate = assemblyDate;
    }

    @Column(name = "casesn", length = 15, unique = true)
    private String caseSn;

    public String getCaseSn() {
        return caseSn;
    }

    public void setCaseSn(String caseSn) {
        this.caseSn = caseSn;
    }

    @Column(name = "qrawsid", length = 30, nullable = false)
    private String qrAwsId;

    @Column(name = "qrfw", length = 30, nullable = false)
    private String qrFw;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "qrburndate", nullable = false)
    private Date qrBurnDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "qrtest1date", nullable = false)
    private Date qrTest1Date;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "qrtest2date", nullable = false)
    private Date qrTest2Date;

    @Column
    @ElementCollection(targetClass = Long.class)
    @OneToMany(mappedBy = "assembly", orphanRemoval = true)
    private Set<AssHistory> history;

    public Set<AssHistory> getHistory() {
        return history;
    }

    public void setHistory(Set<AssHistory> history) {
        this.history = history;
    }

    public String getStringTestDate(){
        return Utils.getFormattedDate(getTestDate());
    }

    public String getStringAssemblyDate(){
        return Utils.getFormattedDate(getAssemblyDate());
    }

    public String getStringQrBurnDate(){
        return Utils.getFormattedDate(getQrBurnDate());
    }

    public String getStringQrTest1Date(){
        return Utils.getFormattedDate(getQrTest1Date());
    }

    public String getStringQrTest2Date(){
        return Utils.getFormattedDate(getQrTest2Date());
    }

    public String getQrAwsId() {
        return qrAwsId;
    }

    public void setQrAwsId(String qrAwsId) {
        this.qrAwsId = qrAwsId;
    }

    public String getQrFw() {
        return qrFw;
    }

    public void setQrFw(String qrFw) {
        this.qrFw = qrFw;
    }

    public Date getQrBurnDate() {
        return qrBurnDate;
    }

    public void setQrBurnDate(Date qrBurnDate) {
        this.qrBurnDate = qrBurnDate;
    }

    public Date getQrTest1Date() {
        return qrTest1Date;
    }

    public void setQrTest1Date(Date qrTest1Date) {
        this.qrTest1Date = qrTest1Date;
    }

    public Date getQrTest2Date() {
        return qrTest2Date;
    }

    public void setQrTest2Date(Date qrTest2Date) {
        this.qrTest2Date = qrTest2Date;
    }

    public List<String> getListForFilter(){
        List<String> filterList = new ArrayList<>();
        filterList.add(getBoardSn());
        filterList.add(getProductName());
        filterList.add(getSimSn());
        filterList.add(getSimPin());
        filterList.add(getQrAwsId());
        filterList.add(getQrFw());
        filterList.add(getCaseSn());
        return filterList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
