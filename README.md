# SLC Budget API

## Description

The Event Manager API is a backend service for the SLC Budget project, designed to handle event management, user registration, contact management, and expense tracking. Built with Java and Spring Boot, this API provides secure and efficient endpoints to manage all the functionalities required by the SLC Budget web application.

SLC Budget API is used by a web application, you can [Try app] or [See code] 

## Features

- User registration and authentication
- Event creation and management
- Contact management
- Activity management within events
- Expense tracking and debt management among participants
- Secure access with JWT-based authentication

## Technologies Used

**Dependencies:**
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Spring Boot Starter Web
- Spring Boot Starter Security
- PostgreSQL
- Lombok
- JSON Web Token (JWT)
- Spring Boot Docker Compose
- Spring Security Test
- Spring Boot Starter Test

**Build Tools:**
- Maven
- Spring Boot Maven Plugin

**Java Version:**
- Java 17

## Getting Started

### Prerequisites

- Java 17
- Maven
- PostgreSQL

### Local

1. Clone the repository:
   ```sh
   git clone https://github.com/your-username/event-manager.git
   ```
2. Build docker image 
   ```sh
   docker build -t slc-budget-api:1.0 .
   ```
3. Run docker container
   ```sh
   docker run -p 8080:8080 slc-budget-api:1.0
   ```

[Try app]: https://github.com/sxngg/SLC-Budget-Website
[See code]: https://main--slc-budget.netlify.app
