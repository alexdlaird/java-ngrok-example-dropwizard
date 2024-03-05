/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard.conf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NgrokConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @JsonProperty
    private String region;

    public boolean isEnabled() {
        return enabled;
    }

    public String getRegion() {
        return region;
    }
}
