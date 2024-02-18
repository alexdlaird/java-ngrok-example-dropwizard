/*
 * Copyright (c) 2023 Alex Laird
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
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.github.alexdlaird.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(DropwizardExtensionsSupport.class)
public class JavaNgrokExampleDropwizardApplicationTest {
    private final static DropwizardAppExtension<JavaNgrokExampleDropwizardConfiguration> dropwizardAppExtension = new DropwizardAppExtension<>(
            JavaNgrokExampleDropwizardApplication.class,
            ResourceHelpers.resourceFilePath("config.yml")
    );

    @Test
    public void testHealthCheck() {
        assumeTrue(isNotBlank(System.getenv("NGROK_AUTHTOKEN")), "NGROK_AUTHTOKEN environment variable not set");

        final Client client = dropwizardAppExtension.client();

        final Response response = client.target(
                        String.format("http://127.0.0.1:%d/healthcheck", dropwizardAppExtension.getAdminPort()))
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
}