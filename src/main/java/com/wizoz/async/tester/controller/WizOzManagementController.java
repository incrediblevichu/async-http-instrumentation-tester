package com.wizoz.async.tester.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wizoz.async.tester.service.WizOzHttpClientService;

@RestController
@RequestMapping("/api/wizOzManagement")
@CrossOrigin()
public class WizOzManagementController {
	
	
	private final WizOzHttpClientService clientService;
	
	public WizOzManagementController(WizOzHttpClientService clientService) {
		this.clientService = clientService;
	}
	
	@GetMapping("/getAvailableClients")
	public ResponseEntity<?> getAvailableClients(){
		return new ResponseEntity<>(clientService.getAvailableClients(), HttpStatus.OK);
	}
	
	@GetMapping("/getDestinationUrl")
	public ResponseEntity<?> getDestinationUrl(){
		return new ResponseEntity<>(clientService.getExitUrl(), HttpStatus.OK);
	}

}
