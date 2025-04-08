package com.ecom.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoyaltyPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDtls user;

    private Double totalPoints = 0.0;
    private Double redeemedPoints = 0.0;
    private Date lastUpdated = new Date();

    // Method to get available points (total - redeemed)
    public Double getAvailablePoints() {
        return totalPoints - redeemedPoints;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserDtls getUser() {
		return user;
	}

	public void setUser(UserDtls user) {
		this.user = user;
	}

	public Double getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(Double totalPoints) {
		this.totalPoints = totalPoints;
	}

	public Double getRedeemedPoints() {
		return redeemedPoints;
	}

	public void setRedeemedPoints(Double redeemedPoints) {
		this.redeemedPoints = redeemedPoints;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
