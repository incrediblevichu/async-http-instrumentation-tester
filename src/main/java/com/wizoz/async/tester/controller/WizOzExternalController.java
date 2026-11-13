package com.wizoz.async.tester.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Default target of the outbound calls. It lets the tester run on its own, with no third-party site involved.
 * To send the outbound calls to another service, set the property {@code wizoz.exit-url}.
 */
@RestController
@RequestMapping("/external")
public class WizOzExternalController {

	private static final Logger log = LoggerFactory.getLogger(WizOzExternalController.class);

	private final ExitRequestRecorder recorder;

	public WizOzExternalController(ExitRequestRecorder recorder) {
		this.recorder = recorder;
	}

	@GetMapping("/{correlationId}")
	public String receive(@PathVariable("correlationId") int correlationId) {
		log.debug("Exit request received for correlation id {}", correlationId);
		recorder.record(correlationId);
		return String.valueOf(correlationId);
	}

}
