package com.github.alexdlaird.ngrok.example.dropwizard.conf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NgrokConfiguration {
    @JsonProperty
    private boolean enabled = false;

    @JsonProperty
    private String authToken;

    @JsonProperty
    private String region;

    public boolean isEnabled() {
        return enabled;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getRegion() {
        return region;
    }
}
