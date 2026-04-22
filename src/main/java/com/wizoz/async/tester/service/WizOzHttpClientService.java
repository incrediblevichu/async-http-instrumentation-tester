package com.wizoz.async.tester.service;

import java.util.Map;

/**
 * 
 * @author Vijay k
 *
 */
public interface WizOzHttpClientService {
	
	/**
	 * 
	 * @param clientType
	 * @param correlationId
	 */
	public void fireRequest(String clientType, int correlationId);
	
	/**
	 * 
	 */
	public String DEFAULT_EXIT_URL = "https://www.google.com/";
	
	/**
	 * 
	 * @return
	 */
	default public String getExitUrl() {
		return DEFAULT_EXIT_URL;
	}
	
	/**
	 * 
	 * @param exitUrl
	 */
	public void setExitUrl(String exitUrl);
	
	
	/**
	 * 
	 */
	public Map<String, String> getAvailableClients();

}
