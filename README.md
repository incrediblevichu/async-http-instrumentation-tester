# async-http-instrumentation-tester

A small test harness for checking that **trace context survives asynchronous HTTP calls**.

APM agents and tracing libraries propagate trace context across threads. Async HTTP clients are where this breaks most often: a callback runs on a different thread, and the context is lost or attached to the wrong request. These bugs depend on timing, so they are hard to reproduce and hard to confirm.

This tool makes them easy to spot by giving every request its own number.

## How it works

Each test request carries a number `i`:

```
inbound:   GET /api/fireRequest/{clientType}/{i}     entry span, correlation id = i
outbound:  GET {exitUrl}{i}                          exit span, made by the async client
```

The application receives inbound request `i` and makes an asynchronous outbound call for the same `i`. If trace context is propagated correctly, the entry span for `i` and the exit span for `i` belong to the same trace.

| What you see in your tracing backend | What it means |
|---|---|
| Exit span `j` in the trace of entry `i`, where `i ≠ j` | Context was **crossed** between requests (thread reuse or hop bug) |
| Exit span `i` with no parent, or in a separate trace | Context was **lost** |
| Entry `i` and exit `i` in the same trace | Context was propagated correctly |

The number is the expected answer, so the check needs no other bookkeeping. It is a black-box check and does not depend on the tracer's internals.

The tool itself does **not** read spans or compare them. You read the result in the tracing backend you are testing (Instana, OpenTelemetry collector, and so on).

## Features

- **Web UI** at `/` where you choose a client, the number of requests (1 to 1000) and the number of concurrent workers (1 to 50). It sends the request numbers `1..N` in random order, spread across the workers, and shows success and failure counts and the average time.
- **Pluggable clients.** Each client is a Spring component. Add one class and it appears in the UI automatically. The UI reads the list from the server, so it needs no rebuild.
- **Self-contained.** Outbound calls go to a built-in `/external/{i}` endpoint by default, so no third-party site is involved.

### Included clients

| Key | Client | Async mechanism |
|---|---|---|
| `vertX` | Vert.x Web Client 5.0.0 | `Future` callbacks |
| `apache` | Apache HttpClient 5 | `FutureCallback` |
| `jdk` | JDK `java.net.http.HttpClient` | `sendAsync` / `CompletableFuture` |
| `okhttp` | OkHttp 4.12.0 | `Call.enqueue` |
| `springWebClient` | Spring `WebClient` (Reactor Netty) | `Mono.subscribe` |
| `jetty` | Jetty `HttpClient` | `send(Response.CompleteListener)` |

## Requirements

- Java 17
- Maven 3.9 or later
- A tracer or APM agent attached to the JVM, to see the spans

## Quick start

```bash
git clone https://github.com/incrediblevichu/async-http-instrumentation-tester.git
cd async-http-instrumentation-tester
mvn package
java -jar target/async-http-instrumentation-tester-0.0.1-SNAPSHOT.jar
```

The application listens on port **12345**. Open http://localhost:12345/ for the UI.

To test a tracer, attach its agent when starting the application, for example:

```bash
java -javaagent:/path/to/agent.jar -jar target/async-http-instrumentation-tester-0.0.1-SNAPSHOT.jar
```

Then, in the UI, pick a client, choose the request count and workers, and press **Fire**. In your tracing backend, check that every number `i` has its entry span and exit span in the same trace.

## Configuration

| Property | Default | Meaning |
|---|---|---|
| `server.port` | `12345` | Port of the application |
| `wizoz.exit-url` | `http://localhost:<server.port>/external/` | Base URL of the outbound calls. The number is appended. A trailing `/` is added if missing |

Example: `java -jar app.jar --wizoz.exit-url=http://my-service:8080/external/`

If you point the exit URL at another service, it must accept `GET {exitUrl}{i}`.

## API

| Endpoint | Purpose |
|---|---|
| `GET /api/fireRequest/{clientType}/{correlationId}` | Fire one async request. Returns 200, or 404 if the client key is unknown |
| `GET /api/wizOzManagement/getAvailableClients` | Registered clients (key and display name) |
| `GET /api/wizOzManagement/getDestinationUrl` | The exit URL in use |
| `GET /external/{correlationId}` | Default exit target. Returns the number |

```bash
curl http://localhost:12345/api/fireRequest/okhttp/42
```

## Adding a new HTTP client

1. Create a class that implements `WizOzAsyncClient` and annotate it with `@Component("yourKey")`:

```java
@Component("myClient")
public class MyAsyncClient implements WizOzAsyncClient {

    @Override
    public void sendAsyncRequest(String exitUrl) {
        // send the request asynchronously and return immediately
    }

    @Override
    public String getClientGenericName() {
        return "My Async Client";
    }
}
```

2. Add the client's library to `pom.xml`.
3. Rebuild and restart the application. The client appears in the UI automatically.

The key (`myClient`) is what you pass in `/api/fireRequest/{clientType}/{i}`.

## Tests

```bash
mvn test
```

The tests check the tester itself, not any tracer. For every registered client, 200 request numbers are fired from 16 threads, and each number must reach the exit endpoint exactly once. They also cover the default exit URL, client registration and the 404 for an unknown client.

## Limitations

- A run only exercises the clients and load you choose. A run with no mismatch shows no problem for that case, not that propagation is correct everywhere.
- Crossed context is more likely at higher concurrency. A small request count can pass by luck.
- Results are read from your tracing backend. There is no built-in span comparison.
- The UI is distributed as a compiled bundle in `src/main/resources/static`. Its source is not in this repository.
- On macOS, Reactor Netty may log a harmless `MacOSDnsServerAddressStreamProvider` error at startup. It falls back to the system DNS resolver.

## License

GPL-3.0. See [LICENSE](LICENSE).

## Author

Vijay K
