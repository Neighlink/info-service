package pe.edu.upc.infoservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jdk.jfr.Category;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Optional;

@Entity
@Table(name = "bills")
@Data
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column
    private String description;
    @Column(nullable = false)
    private float amount;
    @Column(nullable = false)
    private Long condominiumId;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "categoryId", nullable = true)
    private PaymentCategory category;
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "paymentId", nullable = true)
    private Payment payment;
    @Column(nullable = false)
    private Long departmentId;
    @Column(nullable = false)
    private boolean isDelete;

    public void init(String name, String description, Float amount, Date startDate, Date endDate, PaymentCategory category, Long departmentId) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.departmentId = departmentId;
    }
}
