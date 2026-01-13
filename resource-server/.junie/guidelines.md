# Project Guidelines


## Project Structure

This Spring Boot application follows a layered architecture with clear separation of concerns:

```
src/main/java/com/jw/resourceserver/
├── config/                 # Configuration classes
├── controller/             # REST controllers (API endpoints)
│   ├── opened/             # Public endpoints
│   └── secured/            # Authenticated endpoints
├── dto/                    # Data Transfer Objects
│   ├── request/            # Request DTOs
│   ├── response/           # Response DTOs
│   └── security/           # Security-related DTOs
├── entity/                 # JPA entities
│   └── resource/           # Domain entities
├── repository/             # Data access layer
└── service/                # Business logic layer
```

## Design Principles

### Object-Oriented Programming (OOP)

1. **Encapsulation**
   - Keep fields private and provide controlled access through methods
   - Validate input in constructors or setters
   - Use immutable objects (records) for DTOs

2. **Inheritance**
   - Use inheritance for common functionality (e.g., BaseTimeEntity, BaseController)
   - Prefer composition over inheritance where appropriate

3. **Polymorphism**
   - Use interfaces for dependency injection
   - Design for extension through interfaces

4. **Abstraction**
   - Hide implementation details behind interfaces
   - Use abstract base classes for common functionality

### SOLID Principles

1. **Single Responsibility Principle (SRP)**
   - Each class should have only one reason to change
   - Controllers handle HTTP concerns, services handle business logic, repositories handle data access
   - Split large classes into smaller, focused ones

2. **Open/Closed Principle (OCP)**
   - Classes should be open for extension but closed for modification
   - Use inheritance and interfaces to allow extension without modifying existing code
   - Design methods to be overridable when appropriate

3. **Liskov Substitution Principle (LSP)**
   - Subtypes must be substitutable for their base types
   - Override methods without changing the expected behavior
   - Follow the contract defined by parent classes/interfaces

4. **Interface Segregation Principle (ISP)**
   - Clients should not depend on interfaces they don't use
   - Create specific, focused interfaces rather than general-purpose ones
   - Split large interfaces into smaller, more specific ones

5. **Dependency Inversion Principle (DIP)**
   - High-level modules should not depend on low-level modules; both should depend on abstractions
   - Use dependency injection for loose coupling
   - Program to interfaces, not implementations

## Coding Standards

### General

- Follow Java naming conventions
- Use meaningful names for classes, methods, and variables
- Keep methods short and focused on a single task
- Document public APIs with Javadoc

### Classes

- One class per file
- Class names should be nouns in PascalCase (e.g., `BoardService`)
- Use final for class fields where possible
- Initialize collections as empty rather than null

### Methods

- Method names should be verbs in camelCase (e.g., `createBoard`)
- Use the `final` keyword for method parameters
- Validate input parameters at the beginning of methods
- Return empty collections instead of null

### DTOs

- Use Java records for DTOs
- Validate input in compact constructors
- Provide static factory methods for entity-to-DTO conversion
- Include business logic methods relevant to presentation concerns

### Entities

- Use JPA annotations appropriately
- Include domain logic in entity classes
- Use builder pattern for complex object creation
- Extend common base classes for shared fields/behavior

### Exception Handling

- Use specific exceptions rather than generic ones
- Handle exceptions at appropriate levels
- Provide meaningful error messages
- Use @ExceptionHandler in controllers for consistent error responses

## Testing Requirements

- Write unit tests for all service methods
- Use JUnit 5 for testing
- Mock dependencies using Mockito
- Test both success and failure scenarios
- Aim for high test coverage of business logic

### Running Tests

To run tests:

```bash
./gradlew test
```

For specific test classes:

```bash
./gradlew test --tests "com.jw.resourceserver.service.BoardServiceTest"
```

## Build and Deployment

### Building the Project

To build the project:

```bash
./gradlew clean build
```

This will compile the code, run tests, and create an executable JAR file.

### Running the Application

To run the application locally:

```bash
./gradlew bootRun
```

Or using the JAR file:

```bash
java -jar build/libs/resource-server-0.0.1-SNAPSHOT.jar
```

### Environment Configuration

- Use application.yml for configuration
- Use profiles for environment-specific settings
- Externalize sensitive configuration using environment variables