package com.wizoz.async.tester.client.async.impl.jettyHttp;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

/**
 * Async client based on the Jetty {@link HttpClient} (asynchronous listener API).
 */
@Component("jetty")
public class JettyAsyncHttpClient implements WizOzAsyncClient, InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(JettyAsyncHttpClient.class);

	private final HttpClient httpClient = new HttpClient();

	@Override
	public void afterPropertiesSet() throws Exception {
		httpClient.start();
	}

	@Override
	public void destroy() throws Exception {
		httpClient.stop();
	}

	@Override
	public void sendAsyncRequest(String exitUrl) {
		httpClient.newRequest(exitUrl).send(this::onComplete);
	}

	private void onComplete(Result result) {
		if (result.isFailed()) {
			log.error("HTTP call failed: {}", result.getFailure().getMessage(), result.getFailure());
		} else {
			log.debug("HTTP {}", result.getResponse().getStatus());
		}
	}

	@Override
	public String getClientGenericName() {
		return "Jetty HttpClient";
	}

}
