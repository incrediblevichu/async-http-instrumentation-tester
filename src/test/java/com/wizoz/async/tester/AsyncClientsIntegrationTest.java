package com.wizoz.async.tester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.wizoz.async.tester.controller.ExitRequestRecorder;
import com.wizoz.async.tester.service.InvalidExitUrlException;
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

	@AfterEach
	void restoreExitUrl() {
		service.resetExitUrl();
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

	// ---- customer-configurable exit URL ----

	@Test
	void exitUrlCanBeChangedAndNumbersGoToTheNewTarget() throws Exception {
		// "own external service" = a different path on this same server
		String custom = "http://localhost:" + port + "/external";
		assertEquals(custom + "/", service.setExitUrl(custom));
		assertEquals(custom + "/", service.getExitUrl());

		service.fireRequest("jdk", 7);
		awaitCount(7);
		assertEquals(1, recorder.count(7));
	}

	@Test
	void resetRestoresTheStartupUrl() {
		String startup = service.getExitUrl();
		service.setExitUrl("http://example.org/hook/");
		assertEquals("http://example.org/hook/", service.getExitUrl());
		assertEquals(startup, service.resetExitUrl());
	}

	@Test
	void invalidExitUrlsAreRejectedAndTheCurrentUrlIsKept() {
		String before = service.getExitUrl();
		for (String bad : new String[] { null, "", "   ", "localhost:8080/x", "ftp://host/x/", "http://", "not a url",
				"http://host/x/?a=b", "http://host/x/#frag" }) {
			assertThrows(InvalidExitUrlException.class, () -> service.setExitUrl(bad), "should reject: " + bad);
		}
		assertEquals(before, service.getExitUrl());
	}

	@Test
	void setDestinationUrlEndpointAppliesTheUrl() throws Exception {
		HttpResponse<String> response = post("/api/wizOzManagement/setDestinationUrl",
				"{\"url\":\"http://example.org/hook\"}");
		assertEquals(200, response.statusCode());
		assertEquals("http://example.org/hook/", response.body());
		assertEquals("http://example.org/hook/", service.getExitUrl());
	}

	@Test
	void setDestinationUrlEndpointReturns400WithMessageForBadUrl() throws Exception {
		HttpResponse<String> response = post("/api/wizOzManagement/setDestinationUrl", "{\"url\":\"ftp://host/\"}");
		assertEquals(400, response.statusCode());
		assertTrue(response.body().contains("http://"), response.body());
	}

	@Test
	void resetDestinationUrlEndpointRestoresTheStartupUrl() throws Exception {
		String startup = service.getExitUrl();
		service.setExitUrl("http://example.org/hook/");
		HttpResponse<String> response = post("/api/wizOzManagement/resetDestinationUrl", "");
		assertEquals(200, response.statusCode());
		assertEquals(startup, response.body());
	}

	private HttpResponse<String> post(String path, String json) throws Exception {
		return HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(json)).build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private void awaitCount(int id) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		while (recorder.count(id) < 1 && System.nanoTime() < deadline) {
			Thread.sleep(25);
		}
	}

}
