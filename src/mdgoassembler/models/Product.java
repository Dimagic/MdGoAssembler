package mdgoassembler.models;

import javax.persistence.*;

@Entity
@Table(name = "Product", uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class Product {
    private Long id;
    private String name;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", length = 64, nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
