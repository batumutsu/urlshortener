# URL Shortener

[![Java Version](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A robust Spring Boot application that transforms long URLs into concise, shareable short links. Perfect for social media, analytics, and anywhere character count matters.

## 🚀 Features

- **URL Shortening**: Convert lengthy URLs into brief, memorable codes
- **Custom Aliases**: Create personalized short codes (up to 6 characters)
- **Expiration Control**: Set time-to-live (TTL) for temporary links
- **Redirection**: Seamlessly redirect short codes to original URLs
- **Link Management**: Delete shortened URLs when no longer needed

## 🛠️ Tech Stack

- Java 21+
- Spring Boot 3.x
- PostgreSQL
- Maven
- Docker

## 🔗 API Endpoints

### Create Shortened URL

```http
POST /shorten
Content-Type: application/json

{
  "originalUrl": "https://example.com/very/long/url",
  "customId": "my-id",
  "ttl": 3600
}
```

#### Response

```json
{
   "shortUrl": "ab123",
   "originalUrl": "https://example.com/very/long/url",
   "expirationDate": "2024-10-16T10:30:00Z"
}
```

### Redirect to Original URL

```http
GET /{id}
```

### Delete Shortened URL

```http
DELETE /{id}
```

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:
- [Java 21+](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/get-started)
- [PostgreSQL](https://www.postgresql.org/download/) (if running locally)

### Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/batumutsu/urlshortener.git
   cd urlshortener
   ```

2. Set up environment variables (see [Configuration](#-configuration))

3. Build and run with Docker:
   ```bash
   docker-compose up --build
   ```

   Or run locally:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. Access the application at `http://localhost:8080/api/v1`
5. Access the swagger documentation at `http://localhost:8080/api/v1/swagger-ui/index.html`

## ⚙️ Configuration

Set the following environment variables:

```bash
export DEV_POSTGRES_USER=your_username
export DEV_POSTGRES_PASSWORD=your_password
export URL_SHORTENER_DB=url_shortener
export DEV_POSTGRES_PORT=5432
export LOCAL_HOST=localhost
export SPRING_PORT=8080
export SPRING_TEST_PORT=8081
export LOCAL_POSTGRES_BASE_URL=jdbc:postgresql://localhost:5432
export KEY_ALPHABETS=your_key_alphabets
export KEY_LENGTH=6
export APP_CONTEXT_PATH=/api/v1
export DOCKER_POSTGRES_BASE_URL=jdbc:postgresql://db:5432
```

## 🧪 Testing

Run the test suite:

```bash
mvn clean test
```

Or use Docker:

```bash
docker-compose up --build
```

## 📚 Usage Example

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
     -H "Content-Type: application/json" \
     -d '{
       "originalUrl": "http://example.com/very/long/url",
       "customId": "mylink",
       "ttl": 3600
     }'
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, please open an issue in the GitHub repository or contact the maintainers.

---

Happy URL shortening! 🎉