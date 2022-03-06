package com.shopizer.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.system.Environment;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.shopizer.payment.square.autoconfigure.SquarePaymentModule;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SquarePaymentTest {
	
	private static final String CURRENCY = "CAD";
	
	private static final BigDecimal AMOUNT = new BigDecimal("1.00");
	
    @Value("${spring.application.name}")
    String appName;
    
    @Value("${square.applicationId}")
    String applicationId;
    
    @Value("${square.locationId}")
    String locationId;
    
    @Value("${square.accessToken}")
    String accessToken;
    
    @Value("${square.testSourceId}")
    String sourceId;
    
	@Autowired
	private SquarePaymentModule squarePaymentModule;
	
	@Test
	public void contextLoads() {
	}

	
	@Ignore //requires square configuration
	public void testAuthorize() {
		
		IntegrationConfiguration config = prepare();

		MerchantStore store = store();
		
		com.salesmanager.core.model.payments.Payment paymentObject = new com.salesmanager.core.model.payments.Payment();
		paymentObject.setAmount(AMOUNT);
		paymentObject.setCurrency(store.getCurrency());
		paymentObject.setPaymentType(PaymentType.CREDITCARD);
		paymentObject.setTransactionType(TransactionType.AUTHORIZE);
		
		Map<String,String> paymentMetaData = new HashMap<String,String>();
		paymentMetaData.put("source", sourceId);
		paymentObject.setPaymentMetaData(paymentMetaData);
		
		
		try {
	    	/**
	    	 * authorize and capture example
	    	 */
			
			//authorize
			Transaction transaction = squarePaymentModule.authorize(store, null, null, AMOUNT, paymentObject, config, null);
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("id") != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	@Ignore //requires square configuration
	public void testAuthAndCapture() {
		
		IntegrationConfiguration config = prepare();

		MerchantStore store = store();
		
		com.salesmanager.core.model.payments.Payment paymentObject = new com.salesmanager.core.model.payments.Payment();
		paymentObject.setAmount(AMOUNT);
		paymentObject.setCurrency(store.getCurrency());
		paymentObject.setPaymentType(PaymentType.CREDITCARD);
		paymentObject.setTransactionType(TransactionType.AUTHORIZE);
		
		Map<String,String> paymentMetaData = new HashMap<String,String>();
		paymentMetaData.put("source", sourceId);
		paymentObject.setPaymentMetaData(paymentMetaData);
		
		
		try {
	    	/**
	    	 * authorize and capture example
	    	 */
			
			//authorize
			Transaction transaction = squarePaymentModule.authorize(store, null, null, AMOUNT, paymentObject, config, null);
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("id") != null);
			
			//payment
			transaction = squarePaymentModule.capture(store, null, null, transaction, config, null);
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("status").equals("COMPLETED"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Ignore //requires square configuration
	public void testPay() {
		
		IntegrationConfiguration config = prepare();

		MerchantStore store = store();
		
		com.salesmanager.core.model.payments.Payment paymentObject = new com.salesmanager.core.model.payments.Payment();
		paymentObject.setAmount(AMOUNT);
		paymentObject.setCurrency(store.getCurrency());
		paymentObject.setPaymentType(PaymentType.CREDITCARD);
		paymentObject.setTransactionType(TransactionType.AUTHORIZECAPTURE);
		
		Map<String,String> paymentMetaData = new HashMap<String,String>();
		paymentMetaData.put("source", sourceId);
		paymentObject.setPaymentMetaData(paymentMetaData);
		
		
		try {

			//payment
			Transaction transaction = squarePaymentModule.authorizeAndCapture(store, null, null, AMOUNT, paymentObject, config, null);
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("status").equals("CXOMPLETED"));//COMPLETED
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Ignore //requires square configuration
	public void testPayRefund() {
		
		IntegrationConfiguration config = prepare();

		MerchantStore store = store();
		
		com.salesmanager.core.model.payments.Payment paymentObject = new com.salesmanager.core.model.payments.Payment();
		paymentObject.setAmount(AMOUNT);
		paymentObject.setCurrency(store.getCurrency());
		paymentObject.setPaymentType(PaymentType.CREDITCARD);
		paymentObject.setTransactionType(TransactionType.AUTHORIZECAPTURE);
		
		Map<String,String> paymentMetaData = new HashMap<String,String>();
		paymentMetaData.put("source", sourceId);
		paymentObject.setPaymentMetaData(paymentMetaData);
		
		
		try {

			//payment
			Transaction transaction = squarePaymentModule.authorizeAndCapture(store, null, null, AMOUNT, paymentObject, config, null);
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("status").equals("COMPLETED"));
			
			transaction = squarePaymentModule.refund(false, store, transaction, null, AMOUNT, config, null);
			
			assertThat(transaction != null);
			assertThat(transaction.getTransactionDetails().get("status").equals("PENDING"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private IntegrationConfiguration prepare() {
		
		IntegrationConfiguration integration = new IntegrationConfiguration();
		integration.getIntegrationKeys().put("square.applicationId", applicationId);
		integration.getIntegrationKeys().put("square.locationId", locationId);
		integration.getIntegrationKeys().put("square.accessToken", accessToken);
		integration.setActive(true);
		integration.setEnvironment(Environment.TEST.name());
		return integration;
		
	}
	
	private MerchantStore store() {
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		
		Currency c = new Currency();
		c.setCurrency(java.util.Currency.getInstance(CURRENCY));
		store.setCurrency(c);
		return store;
	}

}
