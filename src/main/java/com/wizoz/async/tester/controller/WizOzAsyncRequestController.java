package com.wizoz.async.tester.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wizoz.async.tester.service.WizOzHttpClientService;

@RestController
@RequestMapping("/api/fireRequest")
@CrossOrigin
public class WizOzAsyncRequestController {
	
	private final WizOzHttpClientService clientService;
	
	public WizOzAsyncRequestController(WizOzHttpClientService clientService) {
		this.clientService = clientService;
	}
	
	@GetMapping("/{clientType}/{correlationId}")
	public ResponseEntity<?> fireRequest(@PathVariable(name = "clientType", required = true) String clientType,
			@PathVariable(name = "correlationId",required = true) int correlationId){
		clientService.fireRequest(clientType, correlationId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
