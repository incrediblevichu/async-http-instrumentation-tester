package com.wizoz.async.tester.client.async.impl.apacheHttp;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.slf4j.Logger;


@Component("apache")
public class ApacheAsyncHttpClient implements WizOzAsyncClient {

	private static final Logger log = LoggerFactory.getLogger(ApacheAsyncHttpClient.class);

	private final CloseableHttpAsyncClient httpClient;

	public ApacheAsyncHttpClient(CloseableHttpAsyncClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public void sendAsyncRequest(String exitUrl) {
		SimpleHttpRequest request = SimpleRequestBuilder.get(exitUrl).build();

		httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
			@Override
			public void completed(SimpleHttpResponse response) {
				log.info("HTTP {} Response: {}", response.getCode(), response.getBodyText());
			}

			@Override
			public void failed(Exception ex) {
				log.error("HTTP call failed: {}", ex.getMessage(), ex);
			}

			@Override
			public void cancelled() {
				log.warn("HTTP call cancelled");
			}
		});
	}

	@Override
	public String getClientGenericName() {
		return "Apache Async HTTP Client";
	}

}
