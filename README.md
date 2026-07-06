# Splitwise API (Spring Boot port of the Python console app)

This is the original `SplitWise.py` console tool rewired as a REST API.
Nothing about the *logic* changed — only how input arrives and output leaves.

## How the pieces map

| Python file | Java equivalent | Role |
|---|---|---|
| `Splitwise` class (`expenses`, `users`, methods) | `service/SplitwiseService.java` | Holds state + business logic (a Spring singleton bean) |
| `Expense` inner class | `model/Expense.java` | Plain data holder, now also the JSON shape returned to clients |
| `getPayerName()`, `getPaidAmount()`, `getParticipantsName()`, `getNote()` (all `input()`) | `model/ExpenseRequest.java` | One JSON request body replaces four prompts |
| `addMembers()`'s `input()` loop | `model/MembersRequest.java` | JSON body with a `names` array |
| `while True` menu + `input("Choose an option")` | `controller/SplitwiseController.java` | Each menu number becomes its own HTTP endpoint |
| `print(...)` statements | Return values, serialized to JSON automatically by Spring | |

## Prerequisites

- JDK 17+
- Maven (or use an IDE like IntelliJ/VS Code with the Spring Boot extension, which bundles it)
- A MongoDB instance (local `mongod`, Docker, or MongoDB Atlas) and this in `application.properties`:
  ```
  spring.data.mongodb.uri=mongodb://localhost:27017/splitwise
  ```
  (swap in your Atlas connection string if you're not running Mongo locally)

## Data layer (Mongo)

| Was (in-memory) | Now (Mongo) |
|---|---|
| `users` Map<String, Double> | `members` collection — see `model/Member.java` |
| `expenses` List<Expense> | `expenses` collection — see updated `model/Expense.java` |
| direct map/list mutation in `SplitwiseService` | `repository/MemberRepository.java` + `repository/ExpenseRepository.java`, both `MongoRepository` interfaces Spring implements for you at runtime |

No `@EnableMongoRepositories` annotation was needed — `repository` is a
sub-package of the main application package, so Spring Boot's
auto-configuration finds it automatically.

Data now survives an application restart. You can inspect it directly:
```bash
mongosh splitwise --eval "db.members.find().pretty()"
mongosh splitwise --eval "db.expenses.find().pretty()"
```

## Run it

```bash
cd splitwise-api
mvn spring-boot:run
```

The API comes up on `http://localhost:8080`.

## Endpoints (was: menu options 1–4)

### 1. Add Members — `POST /api/members`
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"names": ["Alice", "Bob", "Carol"]}'
```

### 2. Add Expense — `POST /api/expenses`
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"payer": "Alice", "amount": 120, "participants": ["Bob", "Carol"], "note": "Dinner"}'
```

### List Expenses (bonus, wasn't in the menu loop but existed as a method) — `GET /api/expenses`
```bash
curl http://localhost:8080/api/expenses
```

### 3. Show Outstanding Balance — `GET /api/balances`
```bash
curl http://localhost:8080/api/balances
```

### 4. Simplify Debts — `GET /api/debts/simplify`
```bash
curl http://localhost:8080/api/debts/simplify
```

There's no endpoint for menu option 5 ("Exit") — a REST API doesn't need
one, you just stop making requests.

## A note on the "simplify debts" logic

The original `simplifyDebts()` doesn't actually net balances down to the
minimum number of transactions (which is what "simplify" usually implies
in a real Splitwise-style app) — it just reports, per payer, what each
participant owed *on each expense that payer covered*. This port keeps
that exact behavior so it stays a faithful translation. If you want true
debt simplification (matching each net-lender to each net-borrower with
the fewest transactions), that's a good next feature to add to
`SplitwiseService.simplifyDebts()`.

## Next steps worth considering

- Swap the in-memory `List`/`Map` in `SplitwiseService` for a database
  (Spring Data JPA + H2/Postgres) so data survives a restart.
- Add `@RestControllerAdvice`-based error handling for more cases (already
  started in `GlobalExceptionHandler`).
- Add an "Exit"-equivalent isn't needed, but consider a `DELETE /api/reset`
  endpoint for clearing state between test runs.
