package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class OrderItemLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer productId; // Links to Product.id
    private int limitQuantity; // Maximum quantity per order (e.g., 2 for egg trays)
    private LocalDate startDate; // Start of the limit period
    private LocalDate endDate; // End of the limit period
    private boolean isActive; // Whether the limit is currently enforced

    // Getters, setters, and constructors
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public int getLimitQuantity() { return limitQuantity; }
    public void setLimitQuantity(int limitQuantity) { this.limitQuantity = limitQuantity; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isActive() { return isActive; } // Correct getter for boolean
    public void setActive(boolean active) { this.isActive = active; }
    
    @Override
    public String toString() {
        return "OrderItemLimit{" +
                "id=" + id +
                ", productId=" + productId +
                ", limitQuantity=" + limitQuantity +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                '}';
}
}