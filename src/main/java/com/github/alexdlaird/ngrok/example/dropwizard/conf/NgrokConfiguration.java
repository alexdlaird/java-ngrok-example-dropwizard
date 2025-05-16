/*
 * Copyright (c) 2021-2024 Alex Laird
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.alexdlaird.ngrok.example.dropwizard.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.alexdlaird.ngrok.NgrokClient;

public class NgrokConfiguration {
    @JsonProperty
    private boolean enabled = false;

    private NgrokClient ngrokClient;

    public boolean isEnabled() {
        return enabled;
    }

    public void setNgrokClient(final NgrokClient ngrokClient) {
        this.ngrokClient = ngrokClient;
    }

    public NgrokClient getNgrokClient() {
        return ngrokClient;
    }
}
