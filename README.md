```java
  /**
 * Verifies that the actual query result rows match the list of expected assertions,
 * without assuming any particular row order. Each assertion in {@code expectedAssertions}
 * must match exactly one row from the result set. This method provides detailed error
 * messages for unmatched rows, including the specific assertion failure reason.
 *
 * <p>
 * For each actual row, it tries every remaining expected assertion in order. If one assertion
 * passes without throwing an {@link AssertionError}, it is considered a match and removed from
 * the unmatched list. If no assertion passes, the method fails and reports all the reasons
 * assertions failed for that row.
 * </p>
 *
 * <p>
 * At the end, if there are any unmatched expected assertions left, the method fails with
 * an error indicating how many expected rows did not match any actual row.
 * </p>
 *
 * @param expectedAssertions list of expectations to apply to the result rows, in any order.
 * @return this QueryRunner for method chaining
 * @throws AssertionError if any row doesn't match any expected assertion, or if
 *                        there are remaining unmatched expectations
 */
public QueryRunner expectRowsUnordered(List<Consumer<ResultRow>> expectedAssertions) {
    executeQueryIfNeeded();

    List<ResultRow> actualRows = results.stream()
        .map(ResultRow::new)
        .collect(Collectors.toList());

    List<Consumer<ResultRow>> unmatched = new ArrayList<>(expectedAssertions);
    List<String> assertionErrors = new ArrayList<>();

    actualRows.forEach(actual -> {
        boolean matched = false;

        Iterator<Consumer<ResultRow>> it = unmatched.iterator();
        while (it.hasNext()) {
            Consumer<ResultRow> expected = it.next();
            try {
                expected.accept(actual);
                it.remove();
                matched = true;
                break;
            } catch (AssertionError e) {
                assertionErrors.add("Row: " + actual + "\nReason: " + e.getMessage());
            }
        }

        if (!matched) {
            Assertions.fail("No matching expected assertion found for row:\n" + actual +
                            "\nTried assertions:\n" + String.join("\n\n", assertionErrors));
        }

        assertionErrors.clear();
    });

    if (!unmatched.isEmpty()) {
        Assertions.fail("Some expected assertions did not match any row. Remaining: " + unmatched.size());
    }

    return this;
}
```
