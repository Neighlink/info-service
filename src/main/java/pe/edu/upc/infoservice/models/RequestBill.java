package pe.edu.upc.infoservice.models;

import lombok.Data;

import java.util.Date;

@Data
public class RequestBill {
    private String name;
    private String description;
    private Float amount;
    private Date startDate;
    private Date endDate;
    private Long paymentCategoryId;
}
