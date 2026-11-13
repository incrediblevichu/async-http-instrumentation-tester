package com.wizoz.async.tester.client.async.impl.springWebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

/**
 * Async client based on Spring {@link WebClient} (Reactor Netty).
 */
@Component("springWebClient")
public class SpringWebClientAsyncClient implements WizOzAsyncClient {

	private static final Logger log = LoggerFactory.getLogger(SpringWebClientAsyncClient.class);

	private final WebClient webClient = WebClient.create();

	@Override
	public void sendAsyncRequest(String exitUrl) {
		webClient.get().uri(exitUrl).retrieve().bodyToMono(String.class)
				.subscribe(
						body -> log.debug("HTTP Response: {}", body),
						err -> log.error("HTTP call failed: {}", err.getMessage(), err));
	}

	@Override
	public String getClientGenericName() {
		return "Spring WebClient (Reactor Netty)";
	}

}
