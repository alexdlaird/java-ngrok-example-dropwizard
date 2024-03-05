/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class JavaNgrokExampleDropwizardConfiguration extends Configuration {

    @JsonProperty
    private String environment = "dev";

    @JsonProperty("ngrok")
    private NgrokConfiguration ngrokConfiguration;

    private String publicUrl;

    public String getEnvironment() {
        return environment;
    }

    public NgrokConfiguration getNgrokConfiguration() {
        return ngrokConfiguration;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
}
