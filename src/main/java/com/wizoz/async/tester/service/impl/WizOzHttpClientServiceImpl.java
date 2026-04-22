package com.wizoz.async.tester.service.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;
import com.wizoz.async.tester.service.WizOzHttpClientService;

@Service
public class WizOzHttpClientServiceImpl implements WizOzHttpClientService {
	
	/**
	 * 
	 */
	private static final Logger log = LoggerFactory.getLogger(WizOzHttpClientServiceImpl.class);

	/**
	 * 
	 */
	private final Map<String, WizOzAsyncClient> clientMap;
	
	/**
	 * 
	 */
	private String exitUrl = DEFAULT_EXIT_URL;
	
	/**
	 * 
	 * @param clientMap
	 */
	public WizOzHttpClientServiceImpl(Map<String, WizOzAsyncClient> clientMap) {
		this.clientMap = clientMap;
	}
	
	
	@Override
	public String getExitUrl() {
		return exitUrl;
	}
	
	
	
	/**
	 * 
	 */
	@Override
	public void fireRequest(String clientType, int correlationId) {
		WizOzAsyncClient asyncClient = clientMap.get(clientType);
		if(null == asyncClient) {
			//TODO: throw an exception here and handle it centrally
			log.error("Could not find async client for client type : " + clientType);
			return;
		}
		log.info("Using async client" + asyncClient.getClientGenericName());
		asyncClient.sendAsyncRequest(WizOzHttpClientService.DEFAULT_EXIT_URL+correlationId);
	}


	/**
	 * 
	 */
	@Override
	public void setExitUrl(String exitUrl) {
		this.exitUrl = exitUrl;
	}


	@Override
	public Map<String, String> getAvailableClients() {
		return clientMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().getClientGenericName()));
	}

}
