package com.wizoz.async.tester.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	/**
	 * Sets the URL the outbound requests are sent to. Body: {@code {"url": "http://host:port/path/"}}.
	 * Returns the URL now in use, or 400 if it is not a usable http(s) URL.
	 */
	@PostMapping("/setDestinationUrl")
	public ResponseEntity<String> setDestinationUrl(@RequestBody Map<String, String> body){
		return new ResponseEntity<>(clientService.setExitUrl(body.get("url")), HttpStatus.OK);
	}

	/**
	 * Restores the URL configured at startup. Returns the URL now in use.
	 */
	@PostMapping("/resetDestinationUrl")
	public ResponseEntity<String> resetDestinationUrl(){
		return new ResponseEntity<>(clientService.resetExitUrl(), HttpStatus.OK);
	}

}
