package com.shopizer.payment.square.autoconfigure;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payment.square")
public class SquarePaymentConfigurationProperties {
	
	/**
	 * This will set basic properties to the module if any are required
	 */
	
	//Country
	//US, Canada, Australia, Japan, the United Kingdom, Republic of Ireland, France and Spain
	private List<String> supportedCountry = Arrays.asList("US", "CA", "AU", "JP", "UK", "FR", "ES", "IQ");
	
	private String uniqueCode="payment.square";

	public String getUniqueCode() {
		return uniqueCode;
	}
	
	public List<String> getSupportedCountry() {
		return supportedCountry;
	}
	
	public String getLogo() {
		
		String logo = null;
		try {
			
	        ClassLoader classLoader = getClass().getClassLoader();
	        File inputFile = new File(classLoader
	          .getResource("square.png")
	          .getFile());
	        
	        if(inputFile != null) {

		        byte[] fileContent = FileUtils.readFileToByteArray(inputFile);
		        logo = Base64
		          .getEncoder()
		          .encodeToString(fileContent);
 
	        }
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return logo;
	}



}
