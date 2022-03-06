package com.shopizer.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.shopizer.payment.square.autoconfigure.SquarePaymentModule;

import modules.commons.ModuleStarter;

/**
 * Entry point
 * @author carlsamson
 *
 */
@Controller
public class AppController {
	
    @Value("${spring.application.name}")
    String appName;
    
    @Value("${square.applicationId}")
    String applicationId;
    
    @Value("${square.locationId}")
    String locationId;
    
    @Value("${square.accessToken}")
    String accessToken;
	
	@Autowired
	private List<ModuleStarter> payments = null; // all bound payment module
	
	@Autowired
	private SquarePaymentModule squarePaymentModule;
	
	@PostConstruct
	public void initialize() {
		for(ModuleStarter mod: this.payments) {
			paymentModule(mod);
		}
		
		/**
		 * Validate PaymentModule configuration. Below are required fields
		 */
		
		IntegrationConfiguration integration = new IntegrationConfiguration();
		integration.getIntegrationKeys().put("square.applicationId", applicationId);
		integration.getIntegrationKeys().put("square.locationId", locationId);
		integration.getIntegrationKeys().put("square.accessToken", accessToken);
		
		try {
			squarePaymentModule.validateModuleConfiguration(integration, null);
		} catch (IntegrationException e) {
			e.printStackTrace();
		}
		
	}
	
	private void paymentModule(ModuleStarter starter) {
		//System.out.println("Module " + starter.getUniqueCode());
	}
	

    @RequestMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("applicationId",applicationId);
        model.addAttribute("locationId",locationId);
        return "home";
    }

}
