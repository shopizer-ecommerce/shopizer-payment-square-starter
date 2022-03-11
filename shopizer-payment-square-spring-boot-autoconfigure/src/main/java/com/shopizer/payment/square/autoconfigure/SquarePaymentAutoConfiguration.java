package com.shopizer.payment.square.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.salesmanager.core.modules.integration.payment.model.PaymentModule;

import modules.commons.ModuleStarter;
import modules.commons.ModuleType;

@Configuration
@ConditionalOnClass(PaymentModule.class)
@EnableConfigurationProperties(SquarePaymentConfigurationProperties.class)
public class SquarePaymentAutoConfiguration {
	
	
    private SquarePaymentConfigurationProperties properties;

    public SquarePaymentAutoConfiguration(SquarePaymentConfigurationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SquarePaymentModule squarePaymentModule(){
    	SquarePaymentModule module = new SquarePaymentModule();
    	((ModuleStarter)module).setUniqueCode(this.properties.getUniqueCode());
    	((ModuleStarter)module).setModuleType(ModuleType.PAYMENT);
    	
    	System.out.println(this.properties.getSupportedCountry());
    	System.out.println(this.properties.getLogo());
    	
    	
        return module;
    }

}
