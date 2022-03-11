package com.shopizer.payment.square.autoconfigure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.Environment;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;

import modules.commons.ModuleStarter;
import modules.commons.ModuleType;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SquarePaymentModule implements PaymentModule,ModuleStarter {
	
	private String uniqueCode;
	private ModuleType moduleType;
	
	private String base64Logo;
	private List<String> supportedCountry;
	
	Map<Environment, String> environments  = new HashMap<Environment, String>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
	    put(Environment.PRODUCTION, "https://connect.squareup.com/v2");
	    put(Environment.TEST, "https://connect.squareupsandbox.com/v2");
	}};
	

	@Override
	public void validateModuleConfiguration(IntegrationConfiguration integrationConfiguration, MerchantStore store)
			throws IntegrationException {
		/**
		 * Requires access_token, location and annplicationId
		 */
		
		Validate.notNull(integrationConfiguration, "IntegrationConfiguration is required");
		
		if(!integrationConfiguration.getIntegrationKeys().containsKey("square.applicationId")) {
			throw new IntegrationException("square.applicationId is required");
		}
		
		if(!integrationConfiguration.getIntegrationKeys().containsKey("square.locationId")) {
			throw new IntegrationException("square.locationId is required");
		}
		
		if(!integrationConfiguration.getIntegrationKeys().containsKey("square.accessToken")) {
			throw new IntegrationException("square.accessToken is required");
		}
		
	}

	/** NO INIT TRX FOR SQUARE **/
	@Override
	public Transaction initTransaction(MerchantStore store, Customer customer, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module) throws IntegrationException {
		throw new NotImplementedException();
	}

	@Override
	public Transaction authorize(
			MerchantStore store, 
			Customer customer, 
			List<ShoppingCartItem> items,
			BigDecimal amount, 
			Payment payment, 
			IntegrationConfiguration configuration, 
			IntegrationModule module)
			throws IntegrationException {
		
		Validate.notNull(store,"MerchantStore cannot be null");
		Validate.notNull(store.getCurrency(),"MerchantStore.currency cannot be null");
		validateModuleConfiguration(configuration, store);
		Validate.notNull(payment.getAmount(),"Payment.amount cannot be null");
		Validate.notNull(payment.getPaymentMetaData().get("source"),"source payment metadata is required");
		Validate.notNull(configuration,"Configuration cannot be null");
		Validate.notNull(configuration.getEnvironment(),"Configuration environment cannot be null");
		Validate.notNull(configuration.getIntegrationKeys().get("square.accessToken"),"Configuration token cannot be null");
		
		
		//IntegrationModule not required
		
		//https://developer.squareup.com/explorer/square/payments-api/create-payment
		
		Environment environment= Environment.PRODUCTION;
		if (configuration.getEnvironment().equals("TEST")) {// sandbox
			environment= Environment.TEST;
		}
		
		Transaction t = null;
		
		String url = new StringBuilder().append(environments.get(environment)).append("/payments").toString();
		
		String correlation = UUID.randomUUID().toString();

		ObjectMapper mapper = new ObjectMapper();
		
		//System.out.println(payment.getAmount().toString().replace(".",""));

        Map<String, Object> amnt = new HashMap<String,Object>();
        amnt.put("amount", Integer.parseInt(payment.getAmount().toString().replace(".","")));
        amnt.put("currency", payment.getCurrency().getCode());
        
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("amount_money", amnt);
        payload.put("idempotency_key", correlation);
        payload.put("source_id", payment.getPaymentMetaData().get("source"));
        payload.put("autocomplete", false);//just authorization for a short period of time
        
        try {
			String json = mapper.writeValueAsString(payload);


			/**
			  -H 'Square-Version: 2022-01-20' \
			  -H 'Authorization: Bearer ACCESS_TOKEN' \
			  -H 'Content-Type: application/json' \
			 **/
			
			/**
			 * requires 
			 * 
			 * source -> payment
			 * amount -> input
			 * currency -> Merchant
			 * location_id -> IntegrationConfiguration
			 * reference_id -> payment id
	
			
			{
			    "idempotency_key": "7b0f3ec5-086a-4871-8f13-3c81b3875218",
			    "amount_money": {
			      "amount": 1000,
			      "currency": "USD"
			    },
			    "source_id": "ccof:GaJGNaZa8x4OgDJn4GB",
			    "autocomplete": true,
			    "customer_id": "W92WH6P11H4Z77CTET0RNTGFW8",
			    "location_id": "L88917AVBK2S5",
			    "reference_id": "123456",
			    "note": "Brief description",
			    "app_fee_money": {
			      "amount": 10,
			      "currency": "USD"
			    }
			  }
			 **/
			
			//System.out.println(json);
	        
	        t = this.request(json, url, configuration.getIntegrationKeys().get("square.accessToken"));
		
			t.setAmount(amount);
			t.setTransactionType(TransactionType.AUTHORIZE);
			t.setPaymentType(PaymentType.CREDITCARD);
			t.setTransactionDate(new Date());
			t.getTransactionDetails().put("currecy", payment.getCurrency().getCode());
			t.setDetails(correlation);
		
		} catch (JsonProcessingException e) {
			throw new IntegrationException(e);
		}
		
		return t;
	}

	@Override
	public Transaction capture(MerchantStore store, Customer customer, Order order, Transaction capturableTransaction,
			IntegrationConfiguration configuration, IntegrationModule module) throws IntegrationException {
		
		//https://developer.squareup.com/reference/square/payments-api/complete-payment
		
		/**
		 * Capture (complete payment)
		 */
		
		Validate.notNull(store,"MerchantStore cannot be null");
		Validate.notNull(store.getCurrency(),"MerchantStore.currency cannot be null");
		validateModuleConfiguration(configuration, store);
		Validate.notNull(capturableTransaction,"Previous authorize transaction cannot be null");
		Validate.notNull(capturableTransaction.getTransactionDetails().get("id"),"transaction.id is required");
		Validate.notNull(configuration,"Configuration cannot be null");
		Validate.notNull(configuration.getEnvironment(),"Configuration environment cannot be null");
		Validate.notNull(configuration.getIntegrationKeys().get("square.accessToken"),"Configuration token cannot be null");
	
		Environment environment= Environment.PRODUCTION;
		if (configuration.getEnvironment().equals("TEST")) {// sandbox
			environment= Environment.TEST;
		}
		
		Transaction t = null;
		
		String url = new StringBuilder().append(environments.get(environment)).append("/payments/").append(capturableTransaction.getTransactionDetails().get("id")).append("/complete").toString();
		
		t = this.request("", url, configuration.getIntegrationKeys().get("square.accessToken"));

		t.setTransactionType(TransactionType.CAPTURE);
		t.setPaymentType(PaymentType.CREDITCARD);
		t.setTransactionDate(new Date());
		t.getTransactionDetails().put("currency", capturableTransaction.getTransactionDetails().get("currency"));
		return t;
	}

	@Override
	public Transaction authorizeAndCapture(MerchantStore store, Customer customer, List<ShoppingCartItem> items,
			BigDecimal amount, Payment payment, IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		
		//https://developer.squareup.com/explorer/square/payments-api/create-payment
		
		Validate.notNull(store,"MerchantStore cannot be null");
		Validate.notNull(store.getCurrency(),"MerchantStore.currency cannot be null");
		validateModuleConfiguration(configuration, store);
		Validate.notNull(payment.getAmount(),"Payment.amount cannot be null");
		Validate.notNull(payment.getPaymentMetaData().get("source"),"source payment metadata is required");
		Validate.notNull(configuration,"Configuration cannot be null");
		Validate.notNull(configuration.getEnvironment(),"Configuration environment cannot be null");
		Validate.notNull(configuration.getIntegrationKeys().get("square.accessToken"),"Configuration token cannot be null");
		
		
		//IntegrationModule not required
		
		//https://developer.squareup.com/explorer/square/payments-api/create-payment
		
		Environment environment= Environment.PRODUCTION;
		if (configuration.getEnvironment().equals("TEST")) {// sandbox
			environment= Environment.TEST;
		}
		
		Transaction t = null;
		
		String url = new StringBuilder().append(environments.get(environment)).append("/payments").toString();
		
		String correlation = UUID.randomUUID().toString();

		ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> amnt = new HashMap<String,Object>();
        amnt.put("amount", Integer.parseInt(payment.getAmount().toString().replace(".","")));
        amnt.put("currency", payment.getCurrency().getCode());
        
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("amount_money", amnt);
        payload.put("idempotency_key", correlation);
        payload.put("source_id", payment.getPaymentMetaData().get("source"));
        payload.put("autocomplete", true);
        
        try {
			String json = mapper.writeValueAsString(payload);


			/**
			  -H 'Square-Version: 2022-01-20' \
			  -H 'Authorization: Bearer ACCESS_TOKEN' \
			  -H 'Content-Type: application/json' \
			 **/
			
			/**
			 * requires 
			 * 
			 * source -> payment
			 * amount -> input
			 * currency -> Merchant
			 * location_id -> IntegrationConfiguration
			 * reference_id -> payment id
	
			
			{
			    "idempotency_key": "7b0f3ec5-086a-4871-8f13-3c81b3875218",
			    "amount_money": {
			      "amount": 1000,
			      "currency": "USD"
			    },
			    "source_id": "ccof:GaJGNaZa8x4OgDJn4GB",
			    "autocomplete": true,
			    "customer_id": "W92WH6P11H4Z77CTET0RNTGFW8",
			    "location_id": "L88917AVBK2S5",
			    "reference_id": "123456",
			    "note": "Brief description",
			    "app_fee_money": {
			      "amount": 10,
			      "currency": "USD"
			    }
			  }
			 **/
			
			//System.out.println(json);
	        
	        t = this.request(json, url, configuration.getIntegrationKeys().get("square.accessToken"));
		
			t.setAmount(amount);
			t.setTransactionType(TransactionType.AUTHORIZECAPTURE);
			t.setPaymentType(PaymentType.CREDITCARD);
			t.setTransactionDate(new Date());
			t.getTransactionDetails().put("currecy", payment.getCurrency().getCode());
			t.setDetails(correlation);
		
		} catch (JsonProcessingException e) {
			throw new IntegrationException(e);
		}
		
		return t;
	}

	@Override
	public Transaction refund(boolean partial, MerchantStore store, Transaction transaction, Order order,
			BigDecimal amount, IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		
		//https://developer.squareup.com/reference/square/refunds-api/refund-payment
		
		Validate.notNull(store,"MerchantStore cannot be null");
		Validate.notNull(store.getCurrency(),"MerchantStore.currency cannot be null");
		validateModuleConfiguration(configuration, store);
		Validate.notNull(transaction,"Previous transaction cannot be null");
		Validate.notNull(transaction.getTransactionDetails().get("id"),"transaction.id is required");
		Validate.notNull(configuration,"Configuration cannot be null");
		Validate.notNull(configuration.getEnvironment(),"Configuration environment cannot be null");
		Validate.notNull(configuration.getIntegrationKeys().get("square.accessToken"),"Configuration token cannot be null");
	
		Environment environment= Environment.PRODUCTION;
		if (configuration.getEnvironment().equals("TEST")) {// sandbox
			environment= Environment.TEST;
		}
		
		Transaction t = null;
		
		String url = new StringBuilder().append(environments.get(environment)).append("/refunds").toString();

		String correlation = UUID.randomUUID().toString();

		ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> amnt = new HashMap<String,Object>();
        amnt.put("amount", Integer.parseInt(amount.toString().replace(".","")));
        amnt.put("currency", transaction.getTransactionDetails().get("currency"));
        
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("amount_money", amnt);
        payload.put("idempotency_key", correlation);
        payload.put("payment_id", transaction.getTransactionDetails().get("id"));

        
        try {
			String json = mapper.writeValueAsString(payload);


			/**
			  -H 'Square-Version: 2022-01-20' \
			  -H 'Authorization: Bearer ACCESS_TOKEN' \
			  -H 'Content-Type: application/json' \
			 **/
			
			/**
			 * requires 
			 * 
			 * source -> payment
			 * amount -> input
			 * currency -> Merchant
			 * location_id -> IntegrationConfiguration
			 * reference_id -> payment id
	
			
			{
			    "idempotency_key": "7b0f3ec5-086a-4871-8f13-3c81b3875218",
			    "amount_money": {
			      "amount": 1000,
			      "currency": "USD"
			    },
			    "payment_id": "ccof:GaJGNaZa8x4OgDJn4GB",
			  }
			 **/
			
			//System.out.println(json);
	        
	        t = this.request(json, url, configuration.getIntegrationKeys().get("square.accessToken"));
		
			t.setAmount(amount);
			t.setTransactionType(TransactionType.REFUND);
			t.setPaymentType(PaymentType.CREDITCARD);
			t.setTransactionDate(new Date());
			t.setDetails(correlation);
		
		} catch (JsonProcessingException e) {
			throw new IntegrationException(e);
		}
		
		return t;

	}
	
	private Transaction request(String payload, String url, String token) throws IntegrationException {

		try {
			
		    OkHttpClient client = new OkHttpClient.Builder()
		    	      .build();
			
		    RequestBody body = RequestBody.create(
		    	      MediaType.parse("application/json"), payload);
		    
		    Request request = new Request.Builder()
		    		  .header("Square-Version", "2022-01-20")
		    		  .header("Authorization", "Bearer " + token)
		    		  .header("Content-type", "application/json")
		    	      .url(url)
		    	      .post(body)
		    	      .build();
		    	 
		    Call call = client.newCall(request);
		    Response response = call.execute();
		    
		    if(response.code()!=200) {
		    	throw new IntegrationException(IntegrationException.TRANSACTION_EXCEPTION, response.toString());
		    }
		    

		    String stringResponse = new String(response.body().bytes());
		    Transaction returnTransaction = new Transaction();
		    returnTransaction.setTransactionDate(new Date());

		    JSONParser parser = new JSONParser();
		    JSONObject json = (JSONObject) parser.parse(stringResponse);
		    
		    HashMap<String, String> map = new HashMap<>();
		    
		    handleJSONObject(map, json);
		    
		    returnTransaction.setTransactionDetails(map);

		    return returnTransaction;

	    
		} catch(Exception e) {
			throw new IntegrationException(IntegrationException.TRANSACTION_EXCEPTION, e.getMessage());
		}


	}
	
	private void handleValue(Map<String, String> data, String key, Object value) {
	    if (value instanceof JSONObject) {
	        handleJSONObject(data, (JSONObject) value);
	    } else if (value instanceof JSONArray) {
	        handleJSONArray(data, key, (JSONArray) value);
	    } else {
	        data.put(key, value.toString());
	    	
	    }
	}
	
	@SuppressWarnings("unchecked")
	private void handleJSONObject(Map<String, String> data, JSONObject jsonObject) {
	    jsonObject.keySet().forEach(key -> {
	        Object value = jsonObject.get(key);
	        handleValue(data, key.toString(), value);
	    });
	}
	
	@SuppressWarnings("unchecked")
	private void handleJSONArray(Map<String, String> data, String key, JSONArray jsonArray) {
	    jsonArray.iterator().forEachRemaining(element -> {
	        handleValue(data, key, element);
	    });
	}


	@Override
	public List<String> getConfigurable() {
		List<String> keys = new ArrayList<String>();
				keys.add("square.applicationId");
				keys.add("square.locationId");
				keys.add("square.accessToken");
			return keys;
	}

	@Override
	public String getUniqueCode() {
		return this.uniqueCode;
	}

	@Override
	public void setUniqueCode(String uniqueCode) {
		this.uniqueCode = uniqueCode;
		
	}

	@Override
	public void setModuleType(ModuleType moduleType) {
		this.moduleType=moduleType;
		
	}

	@Override
	public ModuleType getModuleType() {
		return this.moduleType;
	}

	@Override
	public String getLogo() {
		return base64Logo;
	}

	@Override
	public List<String> getSupportedCountry() {
		return supportedCountry;
	}

	@Override
	public void setLogo(String arg0) {
		base64Logo = arg0;
		
	}

	@Override
	public void setSupportedCountry(List<String> arg0) {
		supportedCountry = arg0;
		
	}

}
