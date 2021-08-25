package com.github.alexdlaird;

import com.github.alexdlaird.conf.JavaNgrokExampleDropwizardConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import io.dropwizard.Application;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

public class JavaNgrokExampleDropwizardApplication extends Application<JavaNgrokExampleDropwizardConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(String.valueOf(JavaNgrokExampleDropwizardApplication.class));

    public static void main(final String[] args) throws Exception {
        new JavaNgrokExampleDropwizardApplication().run(args);
    }

    @Override
    public String getName() {
        return "JavaNgrokExampleDropwizard";
    }

    @Override
    public void initialize(final Bootstrap<JavaNgrokExampleDropwizardConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final JavaNgrokExampleDropwizardConfiguration configuration,
                    final Environment environment) {
        // java-ngrok will only be installed, and should only ever be initialized, in a dev environment
        if (configuration.getEnvironment().equals("dev") && configuration.getNgrokConfiguration().isEnabled()) {
            final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
                    .withAuthToken(configuration.getNgrokConfiguration().getAuthToken())
                    .withRegion(nonNull(configuration.getNgrokConfiguration().getRegion()) ? Region.valueOf(configuration.getNgrokConfiguration().getRegion().toUpperCase()) : null)
                    .build();
            final NgrokClient ngrokClient = new NgrokClient.Builder()
                    .withJavaNgrokConfig(javaNgrokConfig)
                    .build();

            final int port = getPort(configuration);

            final CreateTunnel createTunnel = new CreateTunnel.Builder()
                    .withAddr(port)
                    .build();
            final Tunnel tunnel = ngrokClient.connect(createTunnel);

            LOGGER.info(String.format("ngrok tunnel \"%s\" -> \"http://127.0.0.1:%d\"", tunnel.getPublicUrl(), port));

            // Update any base URLs or webhooks to use the public ngrok URL
            initWebhooks(tunnel.getPublicUrl());
        }

        // TODO: implement application
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
}
