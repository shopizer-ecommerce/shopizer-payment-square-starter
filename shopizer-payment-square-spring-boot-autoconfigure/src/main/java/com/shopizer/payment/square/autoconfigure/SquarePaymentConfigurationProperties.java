package com.shopizer.payment.square.autoconfigure;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
			
			Resource resource = new ClassPathResource("square.png");
			if(resource != null) {
				InputStream is = resource.getInputStream();
				if(is != null) {
					byte[] encoded = IOUtils.toByteArray(is);
			        logo = Base64
					          .getEncoder()
					          .encodeToString(encoded);
				}
			}

			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return logo;
	}
	
	public String getModuleConfiguration() {
		
		String moduleConfiguration = null;
		try {
			
			Resource resource = new ClassPathResource("configuration.json");
			if(resource != null) {
				InputStream is = resource.getInputStream();
				if(is != null) {
					byte[] ba = IOUtils.toByteArray(is);
					moduleConfiguration = new String(ba);
				}
			}

			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return moduleConfiguration;
		
	}



}
