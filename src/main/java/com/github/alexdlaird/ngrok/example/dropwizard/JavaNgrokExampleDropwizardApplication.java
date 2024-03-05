/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard;

import com.github.alexdlaird.ngrok.example.dropwizard.conf.JavaNgrokExampleDropwizardConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.example.dropwizard.healthcheck.ServerHealthCheck;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
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

import static java.util.Objects.nonNull;
import static com.github.alexdlaird.util.StringUtils.isNotBlank;

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
        // java-ngrok will only be installed, and should only ever be initialized, in a dev environment
        if (configuration.getEnvironment().equals("dev") &&
                configuration.getNgrokConfiguration().isEnabled() &&
                isNotBlank(System.getenv("NGROK_AUTHTOKEN"))) {
            final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
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
            final String publicUrl = tunnel.getPublicUrl();
            configuration.setPublicUrl(publicUrl);
            initWebhooks(publicUrl);
        }

        // ... The rest of our Dropwizard application
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
