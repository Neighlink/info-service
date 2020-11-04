package pe.edu.upc.infoservice.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;
    @Column(nullable = false)
    private float amount;
    @Column(nullable = false)
    private boolean confirmPaid;
    @Column(nullable = false)
    private Long residentId;

    public void init(Date date, Float amount, Long residentId){
        this.amount = amount;
        this.paymentDate = date;
        this.residentId = residentId;
        this.confirmPaid = false;
    }
}
