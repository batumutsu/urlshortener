# URL Shortener

[![Java Version](https://img.shields.io/badge/Java-21%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A robust Spring Boot application that transforms long URLs into concise, shareable short links. Perfect for social media, analytics, and anywhere character count matters.

# 🚀 Features

- **URL Shortening**: Convert lengthy URLs into brief, memorable codes
- **Custom Aliases**: Create personalized short codes (up to 6 characters)
- **Expiration Control**: Set time-to-live (TTL) for temporary links
- **Redirection**: Seamlessly redirect short codes to original URLs
- **Link Management**: Delete shortened URLs when no longer needed

### **Authenticated Users**
- Can create private short URLs from original URLs
- Have exclusive access to manage their created short URLs
- Can delete their own short URLs
- Receive unique short codes even for previously shortened URLs

### **Non-Authenticated Users**

- Can create public short URLs
- Receive unique short codes for any original URL
- Created URLs are not associated with any user account
- Public short URLs can be accessed by anyone

### **Security Notes**

- Each original URL generates unique short codes for different users
- Private URLs are protected from unauthorized access
- Public URLs maintain existing functionality for backward compatibility

## 🛠️ Tech Stack

- Java 21+
- Spring Boot 3.x
- PostgreSQL
- Maven
- Docker

## 🔗 API Endpoints

### Create user

```http
POST /auth/signup
Content-Type: application/json

{
  "email": "example-email@gmail.com",
  "password": "Password@example123",
  "fullName": "John Doe",
  "role":[USER] (Optional)
}
```

#### Response
```
User example-email@gmail.com was successfully registered.
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "example-email@gmail.com",
  "password": "Password@example123"
}
```

#### Response

```json
{
   "token": "your-token",
   "expiresIn": 3600000
}
```

### Create Shortened URL

- Supports authenticated requests
- Creates private URLs for authenticated users
- Creates public URLs for non-authenticated users

```http
POST /mixed/url/shorten
Content-Type: application/json
Authorization: Bearer your-token-here (Optional)

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

- Authenticated users can access their private URLs
- Public URLs remain accessible to all users

```http
GET /mixed/url/{id}
Authorization: Bearer your-token-here (Optional)
```

### Delete Shortened URL

- Authenticated users can delete their own short URLs
- Public URLs can be deleted by any user

```http
DELETE /mixed/url/{id}
Authorization: Bearer your-token-here (Optional)
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
export JWT_SECRET_KEY=your-secret-key
export JWT_EXPIRATION_TIME=your-choosen-expiration-time
```

## 🧪 Testing

Run the test suite:

```bash
mvn clean test
```

Or use Docker to pull all dependencies, run tests
and start the project:

```bash
docker-compose up --build -d
```

## 📚 Usage Example

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
     -H "Content-Type: application/json" \
     -H 'Authorization: ••••••(Optional)' \
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