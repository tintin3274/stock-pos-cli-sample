package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    private Date date;
    private String id;
    private String name;
    private double price;
    private int quantity;
    private double total;
}
