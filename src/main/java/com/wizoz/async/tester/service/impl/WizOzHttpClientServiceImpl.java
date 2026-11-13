package com.wizoz.async.tester.service.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;
import com.wizoz.async.tester.service.UnknownClientException;
import com.wizoz.async.tester.service.WizOzHttpClientService;

@Service
public class WizOzHttpClientServiceImpl implements WizOzHttpClientService {

	private static final Logger log = LoggerFactory.getLogger(WizOzHttpClientServiceImpl.class);

	/**
	 * All registered clients, keyed by bean name. Spring injects every {@link WizOzAsyncClient} bean, which is how
	 * a new client shows up without any other change.
	 */
	private final Map<String, WizOzAsyncClient> clientMap;

	private final String exitUrl;

	public WizOzHttpClientServiceImpl(Map<String, WizOzAsyncClient> clientMap,
			@Value("${wizoz.exit-url:http://localhost:${server.port:12345}/external/}") String exitUrl) {
		this.clientMap = clientMap;
		this.exitUrl = exitUrl.endsWith("/") ? exitUrl : exitUrl + "/";
	}

	@Override
	public String getExitUrl() {
		return exitUrl;
	}

	@Override
	public void fireRequest(String clientType, int correlationId) {
		WizOzAsyncClient asyncClient = clientMap.get(clientType);
		if (asyncClient == null) {
			throw new UnknownClientException(clientType);
		}
		log.debug("Using async client {} for correlation id {}", asyncClient.getClientGenericName(), correlationId);
		asyncClient.sendAsyncRequest(exitUrl + correlationId);
	}

	@Override
	public Map<String, String> getAvailableClients() {
		return clientMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getClientGenericName()));
	}

}
