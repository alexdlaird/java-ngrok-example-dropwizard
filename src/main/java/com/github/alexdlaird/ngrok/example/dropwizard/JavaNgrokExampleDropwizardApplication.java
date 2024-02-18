/*
 * Copyright (c) 2021 Alex Laird
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.alexdlaird.ngrok.example.dropwizard;

import com.github.alexdlaird.ngrok.example.dropwizard.conf.JavaNgrokExampleDropwizardConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.example.dropwizard.healthcheck.ServerHealthCheck;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import io.dropwizard.Application;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
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
            initWebhooks(tunnel.getPublicUrl());
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
