# Library Management System

A command-line Library Management System implemented in Java 21 using OOP principles.

## Project Structure

```
library-management/
├── pom.xml
├── sample_input.txt
├── src/
│   ├── main/java/com/library/
│   │   ├── Main.java                        # Entry point
│   │   ├── model/
│   │   │   ├── Book.java                    # Book domain model
│   │   │   └── Member.java                  # Member domain model
│   │   ├── service/
│   │   │   └── LibraryService.java          # Core business logic
│   │   ├── command/
│   │   │   └── CommandProcessor.java        # Parses & routes commands
│   │   └── exception/
│   │       └── LibraryException.java        # Domain exception
│   └── test/java/com/library/
│       ├── LibraryServiceTest.java          # Service layer unit tests
│       └── CommandProcessorTest.java        # Command parsing & integration tests
```

## Requirements

- Java 21+
- Maven 3.8+

## Build & Run

```bash
# Build (compiles + runs tests + packages jar)
mvn package

# Run with sample input
java -jar target/library.jar sample_input.txt

# Run tests only
mvn test
```

## Design Decisions

- **`LibraryService`** owns all business logic and state. It throws `LibraryException` for any rule violations.
- **`CommandProcessor`** handles all I/O formatting — it translates raw text lines into service calls and formats output strings. This keeps the service layer clean and easily testable.
- **`Book`** tracks its own copy count. A `Member` holds references to borrowed `Book` objects so displaying member books is a direct list lookup.
- Auto-incremented IDs are managed by `LibraryService` with simple counters — no external ID generation needed.
- Java records are used for the `LibraryStats` return type — a natural fit for a plain data carrier.

## Business Rules Implemented

1. Book and Member IDs start at 1 and auto-increment.
2. Members may borrow at most **3 books** simultaneously.
3. A book with multiple copies tracks each copy independently via `availableCopies`.
4. Borrowing is rejected if: no copies available, member limit reached, or unknown IDs.
5. Returning is rejected if the member did not borrow that book.
