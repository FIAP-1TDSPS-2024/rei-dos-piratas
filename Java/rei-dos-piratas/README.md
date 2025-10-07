## Running the Project with Docker

This project is containerized using Docker and can be built and run easily with Docker Compose. Below are the specific instructions and requirements for this setup:

### Project-Specific Docker Requirements
- **Java Version:** Uses Eclipse Temurin JDK 17 for building and JRE 17 for running the application (as specified in the Dockerfile).
- **Build Tool:** Maven Wrapper (`mvnw`) is used for building the project inside the container.

### Environment Variables
- No required environment variables are set by default in the Dockerfile or `docker-compose.yml`.
- If you need to set custom environment variables, you can create a `.env` file and uncomment the `env_file` line in `docker-compose.yml`.

### Build and Run Instructions
1. **Build and start the application:**
   ```sh
   docker compose up --build
   ```
   This will build the Docker image and start the container for the Java application.

2. **Accessing the application:**
   - The application will be available on port **8080** of your host machine (as mapped in `docker-compose.yml`).

### Special Configuration
- The application runs as a non-root user inside the container for improved security.
- No external services (such as databases or caches) are required or configured by default.
- No persistent volumes or custom networks are defined.

### Ports
- **8080:** The default Spring Boot port is exposed and mapped to the host.

---
_If you need to customize the setup (e.g., add environment variables, connect to external services), update the `docker-compose.yml` accordingly._
