package com.wizoz.async.tester.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request names a client type that is not registered. Results in HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnknownClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownClientException(String clientType) {
		super("No async client registered for client type: " + clientType);
	}

}
