package com.wizoz.async.tester.client.async.impl.jdkHttp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

/**
 * Async client based on the JDK {@link HttpClient} ({@code sendAsync}).
 */
@Component("jdk")
public class JdkAsyncHttpClient implements WizOzAsyncClient {

	private static final Logger log = LoggerFactory.getLogger(JdkAsyncHttpClient.class);

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Override
	public void sendAsyncRequest(String exitUrl) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(exitUrl)).GET().build();
		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
				.whenComplete((response, err) -> {
					if (err != null) {
						log.error("HTTP call failed: {}", err.getMessage(), err);
					} else {
						log.debug("HTTP {} Response: {}", response.statusCode(), response.body());
					}
				});
	}

	@Override
	public String getClientGenericName() {
		return "JDK HttpClient (sendAsync)";
	}

}
