package com.wizoz.async.tester.client.async.impl.apacheHttp;


import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApacheHttpClientConfig {

	@Bean(destroyMethod = "close")
    public CloseableHttpAsyncClient asyncHttpClient() {
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();  // start the client before using
        return client;
    }
}


