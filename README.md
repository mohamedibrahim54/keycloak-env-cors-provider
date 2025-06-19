# Keycloak Environment-based CORS Provider

A Keycloak SPI provider that allows configuring Cross-Origin Resource Sharing (CORS) settings through environment variables.

## Overview

This provider extends Keycloak's CORS functionality by allowing administrators to configure allowed origins through environment variables, making it easier to manage CORS settings in containerized environments like Docker and Kubernetes.

## Features

- Configure allowed CORS origins through environment variables
- Seamlessly integrates with Keycloak's existing CORS mechanism
- Replaces wildcard origins with specific origins from environment variables when needed
- Compatible with Keycloak 26.2.5 and potentially other versions

## Installation

### Standard Installation

1. Build the JAR file:
   ```bash
   mvn clean package
   ```

2. Copy the JAR file to Keycloak's providers directory:
   ```bash
   cp target/keycloak-env-cors-provider-1.0-SNAPSHOT.jar /path/to/keycloak/providers/
   ```

3. Build Keycloak to include the new provider:
   ```bash
   /path/to/keycloak/bin/kc.sh build --spi-cors-provider=env-cors
   ```

4. Start Keycloak with the environment variable for CORS configuration:
   ```bash
   export CORS_ALLOW_ORIGINS="https://app1.example.com,https://app2.example.com"
   /path/to/keycloak/bin/kc.sh start
   ```

### Docker Installation

When using Keycloak in a Docker container, the provider path is:

```
/opt/keycloak/providers
```

Add the following instructions to your Dockerfile:

```dockerfile
# Copy the provider JAR to the Keycloak providers directory
COPY --chown=keycloak:root ./extension/keycloak-env-cors-provider-*.jar /opt/keycloak/providers

# Build Keycloak with the env-cors provider
RUN /opt/keycloak/bin/kc.sh build --spi-cors-provider=env-cors
```

Then, when running your Docker container, make sure to set the `CORS_ALLOW_ORIGINS` environment variable:

```bash
docker run -e CORS_ALLOW_ORIGINS="https://app1.example.com,https://app2.example.com" your-keycloak-image
```

## Configuration

The provider uses the following environment variable:

- `CORS_ALLOW_ORIGINS`: A comma-separated list of allowed origins. For example:
  ```
  CORS_ALLOW_ORIGINS=https://app1.example.com,https://app2.example.com
  ```

## How It Works

The provider implements Keycloak's `CorsFactory` and `Cors` interfaces to provide custom CORS handling. When a wildcard origin (`*`) is configured in Keycloak, the provider will replace it with the specific origins defined in the `CORS_ALLOW_ORIGINS` environment variable.

This is particularly useful in scenarios where:
- You want to restrict CORS to specific origins in production but use a wildcard in development
- You need to dynamically configure CORS origins based on deployment environment
- You're using containerized deployments where configuration through environment variables is preferred

## Requirements

- Java 21
- Keycloak 26.2.5 (may work with other versions, but tested with 26.2.5)
- Maven for building

## Development

This project uses Maven for dependency management and building. The main classes are:

- `EnvCorsFactory`: Implements the `CorsFactory` interface and creates instances of `EnvCors`
- `EnvCors`: Implements the `Cors` interface and provides the custom CORS handling logic

## License

This project is licensed under the MIT License - see below for details:

```
MIT License

Copyright (c) 2025 Mohamed Ibrahim

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Author

Mohamed Ibrahim (https://github.com/mohamedibrahim54)
