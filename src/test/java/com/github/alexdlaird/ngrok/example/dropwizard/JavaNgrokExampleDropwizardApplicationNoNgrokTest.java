/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard;

import com.github.alexdlaird.ngrok.example.dropwizard.conf.JavaNgrokExampleDropwizardConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
public class JavaNgrokExampleDropwizardApplicationNoNgrokTest {
    private final static DropwizardAppExtension<JavaNgrokExampleDropwizardConfiguration> dropwizardAppExtension = new DropwizardAppExtension<>(
            JavaNgrokExampleDropwizardApplication.class,
            ResourceHelpers.resourceFilePath("config.yml"),
            ConfigOverride.config("ngrok.enabled", "false")
    );

    @Test
    public void testHealthCheckNoNgrok() {
        final Client client = dropwizardAppExtension.client();

        final Response response = client.target(
                        String.format("http://127.0.0.1:%d/healthcheck", dropwizardAppExtension.getAdminPort()))
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
}