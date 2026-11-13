package com.wizoz.async.tester.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Counts how many times each correlation id arrived at the built-in {@code /external} endpoint. Lets the tester
 * (and its tests) confirm that every request it fired was delivered exactly once.
 */
@Component
public class ExitRequestRecorder {

	private final Map<Integer, AtomicInteger> counts = new ConcurrentHashMap<>();

	public void record(int correlationId) {
		counts.computeIfAbsent(correlationId, id -> new AtomicInteger()).incrementAndGet();
	}

	public int count(int correlationId) {
		AtomicInteger c = counts.get(correlationId);
		return c == null ? 0 : c.get();
	}

	public void reset() {
		counts.clear();
	}

}
