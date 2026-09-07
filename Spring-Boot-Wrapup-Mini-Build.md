# Day 20 — Spring Boot Wrap-Up + Hands-On Mini Build (3h)

**Project:** BookNook — a Mini Library Lending REST API
**Time cap:** 3 hours, one continuous block, no splitting
**Goal:** Tie together everything from Week 2 (DI, bean scopes, design patterns, Java 8) into one small, working Spring Boot application.

Why this project: it's small enough to finish in 3 hours, but naturally needs a singleton service, a prototype-scoped builder, two design patterns, and five different Java 8 features — so it genuinely "wraps up" the week instead of just being busywork.

---

## Time Budget (stick to this — it's designed to fit exactly)

| Phase | Time | What you do |
|---|---|---|
| 1. Setup | 20 min | Create project, add dependency, verify it runs |
| 2. Domain + Repository | 25 min | `Book` model, in-memory `BookRepository` |
| 3. Patterns: Builder + Strategy | 35 min | `BorrowRequest` builder, `LateFeeStrategy` |
| 4. Service layer (DI + Java 8) | 35 min | `LibraryService` — streams, Optional, lambdas |
| 5. REST Controller | 25 min | Endpoints wired to the service |
| 6. Bean scope demo | 15 min | Prove singleton vs. prototype behavior |
| 7. Test + wrap-up | 25 min | Run curl requests, review checklist |

Total: **180 minutes.** Set a timer per phase if you tend to overrun.

---

## Phase 1 — Setup (20 min)

Generate a project at [start.spring.io](https://start.spring.io) (or use your IDE's initializer) with:
- **Dependencies:** Spring Web only (keep it minimal — no DB needed, we use in-memory storage)
- **Java 17+, Maven, Jar packaging**

`pom.xml` needs just this dependency (the initializer adds it for you):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Main class (generated automatically, just confirm it looks like this):

```java
@SpringBootApplication
public class BookNookApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookNookApplication.class, args);
    }
}
```

Run it. Confirm you see Tomcat start on port 8080 with no errors before moving on.

---

## Phase 2 — Domain + Repository (25 min)

### `Book.java`

```java
public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private boolean borrowed;
    private LocalDate dueDate;

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.borrowed = false;
    }

    // getters + setters for borrowed / dueDate
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
```

### `BookRepository.java` — singleton, holds the in-memory "database"

```java
@Repository
public class BookRepository {

    private final Map<String, Book> books = new ConcurrentHashMap<>();

    @PostConstruct
    public void seed() {
        books.put("001", new Book("001", "Clean Code", "Robert C. Martin"));
        books.put("002", new Book("002", "Effective Java", "Joshua Bloch"));
        books.put("003", new Book("003", "Spring in Action", "Craig Walls"));
    }

    public List<Book> findAll() { return new ArrayList<>(books.values()); }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(books.get(isbn));
    }
}
```

`@Repository` is a `@Component` specialization — Spring registers this as a **singleton** bean automatically, so every part of the app shares the same in-memory map.

---

## Phase 3 — Design Patterns: Builder + Strategy (35 min)

### Builder Pattern — `BorrowRequest.java` (this bean will be **prototype**-scoped)

```java
@Component
@Scope("prototype")
public class BorrowRequest {
    private String isbn;
    private String borrowerName;
    private int loanDays;

    public BorrowRequest withIsbn(String isbn) { this.isbn = isbn; return this; }
    public BorrowRequest withBorrower(String name) { this.borrowerName = name; return this; }
    public BorrowRequest withLoanDays(int days) { this.loanDays = days; return this; }

    public String getIsbn() { return isbn; }
    public String getBorrowerName() { return borrowerName; }
    public int getLoanDays() { return loanDays; }
}
```

Each borrow request needs its **own fresh instance** — that's exactly why this is prototype-scoped rather than singleton (see the bean scope demo in Phase 6).

### Strategy Pattern — pluggable late-fee calculation

```java
public interface LateFeeStrategy {
    double calculateFee(long daysLate);
}

@Component("standardFee")
public class StandardLateFeeStrategy implements LateFeeStrategy {
    public double calculateFee(long daysLate) {
        return daysLate * 0.50; // 50 cents/day
    }
}

@Component("premiumFee")
public class PremiumLateFeeStrategy implements LateFeeStrategy {
    public double calculateFee(long daysLate) {
        return daysLate * 0.20; // discounted rate for premium members
    }
}
```

Two interchangeable algorithms behind one interface — classic Strategy pattern, and it doubles as a second demonstration of dependency injection (Spring injects whichever bean you `@Qualifier` for).

---

## Phase 4 — Service Layer: DI + Java 8 (35 min)

```java
@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final LateFeeStrategy lateFeeStrategy;

    // Constructor injection — preferred: immutable, easy to test
    public LibraryService(BookRepository bookRepository,
                           @Qualifier("standardFee") LateFeeStrategy lateFeeStrategy) {
        this.bookRepository = bookRepository;
        this.lateFeeStrategy = lateFeeStrategy;
    }

    // Java 8: Streams — filter + map + sorted
    public List<String> availableTitles() {
        return bookRepository.findAll().stream()
                .filter(b -> !b.isBorrowed())
                .map(Book::getTitle)              // method reference
                .sorted()
                .collect(Collectors.toList());
    }

    // Java 8: Optional — safe lookup, no null checks
    public String borrow(BorrowRequest request) {
        Book book = bookRepository.findByIsbn(request.getIsbn())
                .filter(b -> !b.isBorrowed())
                .orElseThrow(() -> new IllegalStateException("Book unavailable: " + request.getIsbn()));

        book.setBorrowed(true);
        // Java 8: java.time API
        book.setDueDate(LocalDate.now().plusDays(request.getLoanDays()));

        return String.format("'%s' borrowed by %s, due %s",
                book.getTitle(), request.getBorrowerName(), book.getDueDate());
    }

    // Java 8: Optional + lambda + java.time for late-fee math
    public double calculateLateFee(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .filter(Book::isBorrowed)
                .map(b -> {
                    long daysLate = ChronoUnit.DAYS.between(b.getDueDate(), LocalDate.now());
                    return daysLate > 0 ? lateFeeStrategy.calculateFee(daysLate) : 0.0;
                })
                .orElse(0.0);
    }
}
```

This one class alone demonstrates: constructor injection, `@Qualifier`, Streams, method references, `Optional`, lambdas, and `java.time` — the whole Java 8 half of the revision, applied instead of just recited.

---

## Phase 5 — REST Controller (25 min)

```java
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final LibraryService libraryService;
    private final ApplicationContext context; // used to fetch prototype beans, see Phase 6

    public BookController(LibraryService libraryService, ApplicationContext context) {
        this.libraryService = libraryService;
        this.context = context;
    }

    @GetMapping("/available")
    public List<String> available() {
        return libraryService.availableTitles();
    }

    @PostMapping("/borrow")
    public String borrow(@RequestParam String isbn,
                          @RequestParam String borrower,
                          @RequestParam(defaultValue = "14") int days) {
        // Fetch a FRESH prototype-scoped BorrowRequest every call
        BorrowRequest request = context.getBean(BorrowRequest.class)
                .withIsbn(isbn)
                .withBorrower(borrower)
                .withLoanDays(days);
        return libraryService.borrow(request);
    }

    @GetMapping("/{isbn}/late-fee")
    public double lateFee(@PathVariable String isbn) {
        return libraryService.calculateLateFee(isbn);
    }
}
```

---

## Phase 6 — Prove the Bean Scopes (15 min)

Add a tiny throwaway endpoint to *see* singleton vs. prototype behavior directly, instead of just trusting the theory:

```java
@GetMapping("/scope-check")
public String scopeCheck() {
    BorrowRequest r1 = context.getBean(BorrowRequest.class);
    BorrowRequest r2 = context.getBean(BorrowRequest.class);
    BookRepository repo1 = context.getBean(BookRepository.class);
    BookRepository repo2 = context.getBean(BookRepository.class);

    return "BorrowRequest same instance? " + (r1 == r2) +       // expect: false (prototype)
           " | BookRepository same instance? " + (repo1 == repo2); // expect: true (singleton)
}
```

Hit `GET /api/books/scope-check` and confirm the output reads `false` then `true`. That's the entire Day 19 bean-scope lesson, proven live instead of just described. Delete this endpoint once you've confirmed it (it's a debug tool, not part of the real API).

---

## Phase 7 — Test It (25 min)

Run the app, then from a terminal:

```bash
# List available books
curl http://localhost:8080/api/books/available

# Borrow a book
curl -X POST "http://localhost:8080/api/books/borrow?isbn=001&borrower=Alex&days=1"

# Check available again — 001 should be gone
curl http://localhost:8080/api/books/available

# Manually set the system clock forward mentally — or shrink loan days to 0
# then check the late fee:
curl http://localhost:8080/api/books/001/late-fee

# Confirm bean scopes:
curl http://localhost:8080/api/books/scope-check
```

If all four calls return sensible output with no stack traces, the build works end to end.

---

## Wrap-Up Checklist

Check each box as you confirm it — this is the actual "wrap-up" the task title refers to.

- [ ] App starts cleanly with Spring Web only, no DB
- [ ] `BookRepository` is a singleton — confirmed via `/scope-check`
- [ ] `BorrowRequest` is prototype-scoped — confirmed via `/scope-check`
- [ ] Builder pattern used to construct `BorrowRequest` fluently
- [ ] Strategy pattern used for swappable late-fee logic
- [ ] Constructor injection used throughout (no field injection)
- [ ] Streams used to filter/map/sort available titles
- [ ] `Optional` used for the "book might not exist" lookup
- [ ] Method reference (`Book::getTitle`) used at least once
- [ ] `java.time` (`LocalDate`, `ChronoUnit`) used for due dates and late fees
- [ ] All four curl tests pass

Once every box is checked, the mini-build has genuinely exercised bean scopes, two design patterns, dependency injection, and five Java 8 features — the full scope of the task — and you're done. Go check off task #20 in TickTick.
