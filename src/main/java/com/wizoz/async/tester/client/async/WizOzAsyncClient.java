package com.wizoz.async.tester.client.async;

/**
 *<h1>
 *This interface acts as the base interface of asynchronous HTTP clients. All client implementations should implement
 *this interface
 *</h1>
 * @author Vijay k
 * @version 1.0
 */
public interface WizOzAsyncClient {
	
	/**
	 * This method will send the asynchronous HTTP request.
	 * Request should return immediately after sending the request.
	 * 
	 * @param exitRequestId 
	 */
	public void sendAsyncRequest(String exitUrl);
	
	
	/**
	 * This method returns the generic name of the HTTP client
	 * @return
	 */
	public String getClientGenericName();
	
}
