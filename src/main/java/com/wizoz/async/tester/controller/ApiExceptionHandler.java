package com.wizoz.async.tester.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wizoz.async.tester.service.InvalidExitUrlException;
import com.wizoz.async.tester.service.UnknownClientException;

/**
 * Turns the tester's own exceptions into JSON bodies of the form {@code {"message": "..."}}, which the UI shows.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(InvalidExitUrlException.class)
	public ResponseEntity<Map<String, String>> invalidExitUrl(InvalidExitUrlException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
	}

	@ExceptionHandler(UnknownClientException.class)
	public ResponseEntity<Map<String, String>> unknownClient(UnknownClientException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
	}

}
