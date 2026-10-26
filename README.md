[![Build](https://img.shields.io/github/actions/workflow/status/alexdlaird/java-ngrok-example-dropwizard/build.yml)](https://github.com/alexdlaird/java-ngrok-example-dropwizard/actions/workflows/build.yml)
![GitHub License](https://img.shields.io/github/license/alexdlaird/java-ngrok-example-dropwizard)

# java-ngrok Example - Dropwizard

This is an example project that shows how to easily integrate [`java-ngrok`](https://github.com/alexdlaird/java-ngrok)
with [Dropwizard](https://www.dropwizard.io/en/latest/index.html), as [summarized in the docs](https://alexdlaird.github.io/java-ngrok/integration/#dropwizard).

## Running

To run the example locally, build it, then start the server with the config file, which sets `ngrok.enabled` to open a
tunnel. The tunnel's public URL is printed when the server starts.

```sh
make build
NGROK_AUTHTOKEN=$NGROK_AUTHTOKEN java -jar target/java-ngrok-example-dropwizard-1.0.0-SNAPSHOT.jar server src/main/resources/config.yml
```

To run the tests, use `make test`. Tests that open a tunnel are skipped if `NGROK_AUTHTOKEN` isn't set.
