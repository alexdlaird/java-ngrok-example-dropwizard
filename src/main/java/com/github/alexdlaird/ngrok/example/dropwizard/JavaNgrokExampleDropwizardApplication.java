/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard;

import com.github.alexdlaird.ngrok.example.dropwizard.conf.JavaNgrokExampleDropwizardConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.example.dropwizard.healthcheck.ServerHealthCheck;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import io.dropwizard.core.Application;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.SimpleServerFactory;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;

import java.util.logging.Logger;
import java.util.stream.Stream;

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
        // ... Initialize our Dropwizard application
        bootstrap.getHealthCheckRegistry().register("server", new ServerHealthCheck());
    }

    @Override
    public void run(final JavaNgrokExampleDropwizardConfiguration configuration,
                    final Environment environment) {
        // Only install and initialize ngrok if we're actually using it
        if (configuration.getEnvironment().equals("dev") &&
                configuration.getNgrokConfiguration().isEnabled()) {
            final NgrokClient ngrokClient = new NgrokClient.Builder()
                    .build();
            configuration.getNgrokConfiguration().setNgrokClient(ngrokClient);

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

        // ... Implement the rest of our Dropwizard application
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
        // ... Implement updates necessary so inbound traffic uses the public-facing ngrok URL
    }
}
