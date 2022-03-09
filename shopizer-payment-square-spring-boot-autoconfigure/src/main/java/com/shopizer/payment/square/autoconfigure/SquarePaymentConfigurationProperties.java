package com.shopizer.payment.square.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payment.square")
public class SquarePaymentConfigurationProperties {
	
	/**
	 * This will set basic properties to the module if any are required
	 */
	
	//Country
	//US, Canada, Australia, Japan, the United Kingdom, Republic of Ireland, France and Spain

	
	private String uniqueCode="payment.square";

	public String getUniqueCode() {
		return uniqueCode;
	}



}
