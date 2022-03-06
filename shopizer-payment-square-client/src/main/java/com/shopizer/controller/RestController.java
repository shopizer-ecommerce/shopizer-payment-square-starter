package com.shopizer.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.core.model.system.Environment;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.shopizer.payment.square.autoconfigure.SquarePaymentModule;


@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/payment")
public class RestController {
	
	private static final String CURRENCY = "CAD";
	
	@Autowired
	private SquarePaymentModule squarePaymentModule;
	
    @Value("${square.accessToken}")
    String accessToken;
    
    @Value("${square.applicationId}")
    String applicationId;
    
    @Value("${square.locationId}")
    String locationId;
	
    @PostMapping(produces = { APPLICATION_JSON_VALUE })
    public ResponseEntity<String> pay(@RequestBody Payment payment) {

    	
		MerchantStore store = new MerchantStore();
		store.setCode("DEFAULT");
		
		Currency c = new Currency();
		c.setCurrency(java.util.Currency.getInstance(CURRENCY));
		store.setCurrency(c);
		
		/**
		 * 	MerchantStore store, 
			Customer customer, 
			List<ShoppingCartItem> items,
			BigDecimal amount, 
			Payment payment, 
			IntegrationConfiguration configuration, 
			IntegrationModule module)
			throws IntegrationException {
		 */
		
		com.salesmanager.core.model.payments.Payment paymentObject = new com.salesmanager.core.model.payments.Payment();
		paymentObject.setAmount(payment.getAmount());
		paymentObject.setCurrency(c);
		paymentObject.setPaymentType(PaymentType.CREDITCARD);
		paymentObject.setTransactionType(TransactionType.AUTHORIZE);
		
		Map<String,String> paymentMetaData = new HashMap<String,String>();
		paymentMetaData.put("source", payment.getSourceId());
		paymentObject.setPaymentMetaData(paymentMetaData);
		
		IntegrationConfiguration config = new IntegrationConfiguration();
		config.setActive(true);
		config.setEnvironment(Environment.TEST.name());
		config.getIntegrationKeys().put("square.accessToken", accessToken);
		config.getIntegrationKeys().put("square.applicationId", applicationId);
		config.getIntegrationKeys().put("square.locationId", locationId);

		
		try {
	    	/**
	    	 * authorize and capture example
	    	 */
			
			//authorize
			Transaction transaction = squarePaymentModule.authorize(store, null, null, payment.getAmount(), paymentObject, config, null);
		
		
			String paymentId = transaction.getTransactionDetails().get("id");
			Validate.notNull(paymentId, "Payment Id Is Null");
			
			//capture
			transaction = squarePaymentModule.capture(store, null, null, transaction, config, null);

		} catch (Exception e) {
			e.printStackTrace();
			String error = String.format("{\"message\":\"%s\"}", e.getMessage());
			return ResponseEntity
					.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(error);
		}
		
		String message = String.format("{\"message\":\"%s\"}", "success");
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(message);

    }

}
