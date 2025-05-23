[![Build](https://img.shields.io/github/actions/workflow/status/alexdlaird/java-ngrok-example-dropwizard/build.yml)](https://github.com/alexdlaird/java-ngrok-example-dropwizard/actions/workflows/build.yml)
![GitHub License](https://img.shields.io/github/license/alexdlaird/java-ngrok-example-dropwizard)

# java-ngrok Example - Dropwizard

This is an example project that shows how to easily integrate [`java-ngrok`](https://github.com/alexdlaird/java-ngrok)
with [Dropwizard](https://www.dropwizard.io/en/latest/index.html).

## Configuration

Create
a [`NgrokConfiguration`](https://github.com/alexdlaird/java-ngrok-example-dropwizard/blob/main/src/main/java/com/github/alexdlaird/conf/NgrokConfiguration.java)
class that lets you use the config to enable `ngrok` and pass it some useful parameters.

```java
public class NgrokConfiguration {
    @JsonProperty
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }
}
```

Then wire this class as a `JsonProperty`
to [the Dropwizard Configuration for your Application](https://www.dropwizard.io/en/latest/getting-started.html#creating-a-configuration-class).

```java
public class JavaNgrokExampleDropwizardConfiguration extends Configuration {
    @JsonProperty
    private String environment = "dev";

    @JsonProperty("ngrok")
    private NgrokConfiguration ngrokConfiguration;

    public String getEnvironment() {
        return environment;
    }

    public NgrokConfiguration getNgrokConfiguration() {
        return ngrokConfiguration;
    }
}
```

And pass parameters to your Dropwizard application through
[your config file](https://github.com/alexdlaird/java-ngrok-example-dropwizard/blob/main/config.yml):

```yaml
ngrok:
  enabled: true
```

## Application Integration

If `ngrok.enabled` config flag is set, `java-ngrok` will be initialized when Dropwizard is booting. An easy place to do
this is in the `run()` method of [the Application](https://github.com/alexdlaird/java-ngrok-example-dropwizard/blob/main/src/main/java/com/github/alexdlaird/JavaNgrokExampleDropwizardApplication.java).

```java
public class JavaNgrokExampleDropwizardApplication extends Application<JavaNgrokExampleDropwizardConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(String.valueOf(JavaNgrokExampleDropwizardApplication.class));

    @Override
    public void run(final JavaNgrokExampleDropwizardConfiguration configuration,
                    final Environment environment) {
        // java-ngrok will only be installed, and should only ever be initialized, in a dev environment
        if (configuration.getEnvironment().equals("dev") &&
                configuration.getNgrokConfiguration().isEnabled()) {
            final NgrokClient ngrokClient = new NgrokClient.Builder()
                    .build();

            final int port = getPort(configuration);

            final CreateTunnel createTunnel = new CreateTunnel.Builder()
                    .withAddr(port)
                    .build();
            final Tunnel tunnel = ngrokClient.connect(createTunnel);
            final String publicUrl = tunnel.getPublicUrl();

            LOGGER.info(String.format("ngrok tunnel \"%s\" -> \"http://127.0.0.1:%d\"", publicUrl, port));

            // Update any base URLs or webhooks to use the public ngrok URL
            configuration.setPublicUrl(publicUrl);
            initWebhooks(publicUrl);
        }
    }

    private int getPort(JavaNgrokExampleDropwizardConfiguration configuration) {
        final Stream<ConnectorFactory> connectors = configuration.getServerFactory() instanceof DefaultServerFactory
                ? ((DefaultServerFactory) configuration.getServerFactory()).getApplicationConnectors().stream()
                : Stream.of((SimpleServerFactory) configuration.getServerFactory()).map(SimpleServerFactory::getConnector);

        return connectors.filter(connector -> connector.getClass().isAssignableFrom(HttpConnectorFactory.class))
                .map(connector -> (HttpConnectorFactory) connector)
                .mapToInt(HttpConnectorFactory::getPort)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void initWebhooks(final String publicUrl) {
        // Update inbound traffic via APIs to use the public-facing ngrok URL
    }

    // ... The rest of your Dropwizard application
}
```

Now Dropwizard can be started by the usual means, setting `ngrok.enabled` in the config to open a tunnel.

1. Run `make build` to build the application
1. Start application with `java -jar target/java-ngrok-example-dropwizard-1.0.0-SNAPSHOT.jar server src/main/resources/config.yml`
1. Check the logs for the `ngrok` tunnel's public URL, which should tunnel to  `http://localhost:8080`
