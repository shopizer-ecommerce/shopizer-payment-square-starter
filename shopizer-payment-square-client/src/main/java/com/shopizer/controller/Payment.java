package com.shopizer.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Payment {
	
	private String locationId;
	private String sourceId;
	private BigDecimal amount = new BigDecimal("1.00");
	
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public BigDecimal getAmount() {
		BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_EVEN);
		return rounded;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
