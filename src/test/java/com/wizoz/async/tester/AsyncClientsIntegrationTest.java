package com.wizoz.async.tester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.wizoz.async.tester.controller.ExitRequestRecorder;
import com.wizoz.async.tester.service.UnknownClientException;
import com.wizoz.async.tester.service.WizOzHttpClientService;

/**
 * Checks the tester itself: for every registered client, each of N request numbers fired concurrently must reach
 * the exit endpoint exactly once.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AsyncClientsIntegrationTest {

	private static final int REQUESTS = 200;
	private static final int THREADS = 16;

	private static int port;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			port = socket.getLocalPort();
		}
		registry.add("server.port", () -> port);
	}

	@Autowired
	private WizOzHttpClientService service;

	@Autowired
	private ExitRequestRecorder recorder;

	@BeforeEach
	void resetRecorder() {
		recorder.reset();
	}

	@Test
	void exitUrlDefaultsToLocalExternalEndpoint() {
		assertEquals("http://localhost:" + port + "/external/", service.getExitUrl());
	}

	@Test
	void allExpectedClientsAreRegistered() {
		Map<String, String> clients = service.getAvailableClients();
		for (String key : List.of("vertX", "apache", "jdk", "okhttp", "springWebClient", "jetty")) {
			assertTrue(clients.containsKey(key), "missing client: " + key);
		}
	}

	@Test
	void everyClientDeliversEachCorrelationIdExactlyOnceUnderConcurrency() throws Exception {
		for (String client : service.getAvailableClients().keySet()) {
			recorder.reset();
			fireConcurrently(client);
			awaitAllDelivered(client);
			for (int id = 1; id <= REQUESTS; id++) {
				assertEquals(1, recorder.count(id), client + ": correlation id " + id + " delivered wrong number of times");
			}
		}
	}

	@Test
	void unknownClientIsRejected() {
		try {
			service.fireRequest("doesNotExist", 1);
			throw new AssertionError("expected UnknownClientException");
		} catch (UnknownClientException expected) {
			// expected
		}
	}

	@Test
	void unknownClientReturns404OverHttp() throws Exception {
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/fireRequest/doesNotExist/1")).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(404, response.statusCode());
	}

	@Test
	void fireRequestEndpointReturns200ForKnownClient() throws Exception {
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/fireRequest/jdk/1")).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
	}

	private void fireConcurrently(String client) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		try {
			List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
			for (int id = 1; id <= REQUESTS; id++) {
				final int correlationId = id;
				futures.add(pool.submit(() -> service.fireRequest(client, correlationId)));
			}
			for (java.util.concurrent.Future<?> f : futures) {
				f.get(30, TimeUnit.SECONDS);
			}
		} finally {
			pool.shutdownNow();
		}
	}

	private void awaitAllDelivered(String client) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		while (System.nanoTime() < deadline) {
			boolean all = true;
			for (int id = 1; id <= REQUESTS && all; id++) {
				all = recorder.count(id) >= 1;
			}
			if (all) {
				return;
			}
			Thread.sleep(50);
		}
		assertFalse(true, client + ": not all requests arrived within 30 seconds");
	}

}
