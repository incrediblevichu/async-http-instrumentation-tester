package com.wizoz.async.tester.service;

import java.util.Map;

/**
 * Fires asynchronous HTTP requests through the registered {@link com.wizoz.async.tester.client.async.WizOzAsyncClient}
 * implementations.
 *
 * @author Vijay k
 */
public interface WizOzHttpClientService {

	/**
	 * Fires one asynchronous request. The outbound request goes to {@link #getExitUrl()} followed by the
	 * correlation id.
	 *
	 * @param clientType    key of the client (the bean name, as returned by {@link #getAvailableClients()})
	 * @param correlationId number that identifies this request end to end
	 * @throws com.wizoz.async.tester.service.UnknownClientException if no client is registered under that key
	 */
	void fireRequest(String clientType, int correlationId);

	/**
	 * @return the base URL that outbound requests are sent to. Always ends with "/".
	 */
	String getExitUrl();

	/**
	 * Changes the base URL that outbound requests are sent to. Takes effect for the next request.
	 *
	 * @param exitUrl absolute http or https URL. A trailing "/" is added if missing. The correlation id is appended
	 *                to it, so it should end with a path that accepts one more segment
	 * @return the URL now in use
	 * @throws com.wizoz.async.tester.service.InvalidExitUrlException if the URL is not an absolute http(s) URL
	 */
	String setExitUrl(String exitUrl);

	/**
	 * Restores the exit URL that was configured at startup (property {@code wizoz.exit-url}, or the built-in
	 * local endpoint).
	 *
	 * @return the URL now in use
	 */
	String resetExitUrl();

	/**
	 * @return map of client key to its display name
	 */
	Map<String, String> getAvailableClients();

}
