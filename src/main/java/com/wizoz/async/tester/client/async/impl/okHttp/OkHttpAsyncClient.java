package com.wizoz.async.tester.client.async.impl.okHttp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Async client based on OkHttp ({@code Call.enqueue}).
 */
@Component("okhttp")
public class OkHttpAsyncClient implements WizOzAsyncClient {

	private static final Logger log = LoggerFactory.getLogger(OkHttpAsyncClient.class);

	private final OkHttpClient httpClient = new OkHttpClient();

	@Override
	public void sendAsyncRequest(String exitUrl) {
		Request request = new Request.Builder().url(exitUrl).build();
		httpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try (response) {
					log.debug("HTTP {} Response: {}", response.code(), response.body().string());
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				log.error("HTTP call failed: {}", e.getMessage(), e);
			}
		});
	}

	@Override
	public String getClientGenericName() {
		return "OkHttp (enqueue)";
	}

}
