package com.wizoz.async.tester.client.async.impl.vertX;



import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wizoz.async.tester.client.async.WizOzAsyncClient;

import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

import org.slf4j.Logger ;


/**
 * This class implements the Vert.X asynchronous client
 * @author Vijay k
 *
 */
@Component("vertX")
public class VertXAsyncClient implements WizOzAsyncClient {
	
	/**
	 * 
	 */
	private static final Logger log = LoggerFactory.getLogger(VertXAsyncClient.class);
	
	/**
	 * 
	 */
    private final WebClient webClient;

    /**
     * 
     * @param webClient
     */
    public VertXAsyncClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 
     */
	@Override
	public void sendAsyncRequest(String exitUrl) {
		webClient.getAbs(exitUrl)
        .as(BodyCodec.string())
        .send()
        .onSuccess(response -> {
            log.debug("HTTP {} Response: {}", response.statusCode(), response.body());
        })
        .onFailure(err -> {
            log.error("HTTP call failed: {}", err.getMessage(), err);
        });

	}

	/**
	 * 
	 */
	@Override
	public String getClientGenericName() {
		return "Async Vert.X";
	}

}
