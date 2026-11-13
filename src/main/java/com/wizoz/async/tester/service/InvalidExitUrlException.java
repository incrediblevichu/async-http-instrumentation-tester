package com.wizoz.async.tester.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested exit URL is not usable. Results in HTTP 400.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidExitUrlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidExitUrlException(String message) {
		super(message);
	}

}
