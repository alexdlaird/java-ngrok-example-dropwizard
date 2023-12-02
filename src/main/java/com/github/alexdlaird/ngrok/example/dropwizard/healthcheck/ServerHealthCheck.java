package com.github.alexdlaird.ngrok.example.dropwizard.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class ServerHealthCheck extends HealthCheck {
    @Override
    protected Result check() {
        return Result.healthy();
    }
}
