package com.github.alexdlaird.ngrok.example.dropwizard;

import com.github.alexdlaird.ngrok.example.dropwizard.conf.JavaNgrokExampleDropwizardConfiguration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
class JavaNgrokExampleDropwizardApplicationTest {
    private static DropwizardAppExtension<JavaNgrokExampleDropwizardConfiguration> EXT = new DropwizardAppExtension<>(
            JavaNgrokExampleDropwizardApplication.class,
            ResourceHelpers.resourceFilePath("config.yml")
    );

    @Test
    void testHealthCheck() {
        Client client = EXT.client();

        Response response = client.target(
                        String.format("http://localhost:%d/healthcheck", EXT.getAdminPort()))
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
}