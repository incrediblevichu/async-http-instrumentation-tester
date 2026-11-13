package com.wizoz.async.tester.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;
import com.wizoz.async.tester.service.InvalidExitUrlException;
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

	/** URL configured at startup, used by {@link #resetExitUrl()}. */
	private final String defaultExitUrl;

	/** Current URL. Volatile because it is read by request threads and changed from the management API. */
	private volatile String exitUrl;

	public WizOzHttpClientServiceImpl(Map<String, WizOzAsyncClient> clientMap,
			@Value("${wizoz.exit-url:http://localhost:${server.port:12345}/external/}") String exitUrl) {
		this.clientMap = clientMap;
		this.defaultExitUrl = normalise(exitUrl);
		this.exitUrl = this.defaultExitUrl;
	}

	private static String normalise(String url) {
		return url.endsWith("/") ? url : url + "/";
	}

	@Override
	public String setExitUrl(String newExitUrl) {
		if (newExitUrl == null || newExitUrl.isBlank()) {
			throw new InvalidExitUrlException("The exit URL must not be empty.");
		}
		String trimmed = newExitUrl.trim();
		URI uri;
		try {
			uri = new URI(trimmed);
		} catch (URISyntaxException e) {
			throw new InvalidExitUrlException("Not a valid URL: " + e.getReason());
		}
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			throw new InvalidExitUrlException("The exit URL must start with http:// or https://");
		}
		if (uri.getHost() == null || uri.getHost().isBlank()) {
			throw new InvalidExitUrlException("The exit URL must contain a host name.");
		}
		if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
			throw new InvalidExitUrlException(
					"The exit URL must not contain a query or fragment, because the number is appended to the path.");
		}
		this.exitUrl = normalise(trimmed);
		log.info("Exit URL changed to {}", this.exitUrl);
		return this.exitUrl;
	}

	@Override
	public String resetExitUrl() {
		this.exitUrl = defaultExitUrl;
		log.info("Exit URL reset to {}", this.exitUrl);
		return this.exitUrl;
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
