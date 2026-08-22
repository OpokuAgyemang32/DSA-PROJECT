package hospital;

/**
 * Minimal self-contained test harness so the module can be unit-tested without
 * pulling in JUnit (no build tool has been agreed on for the team yet). Once
 * Role 3/4 or the team settles on Maven/Gradle, these can be ported to
 * @Test-annotated JUnit methods with very little change - each check() call
 * below maps directly to an assertTrue/assertEquals.
 *
 * Usage pattern in a test class's main():
 *   TestRunner t = new TestRunner("SearchAlgorithmsTest");
 *   t.check("linear search finds present element", ... );
 *   t.summary();
 */
public class TestRunner {

    private final String suiteName;
    private int passed = 0;
    private int failed = 0;

    public TestRunner(String suiteName) {
        this.suiteName = suiteName;
    }

    public void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + description);
        } else {
            failed++;
            System.out.println("  [FAIL] " + description);
        }
    }

    /** Use for tests that assert an exception is thrown (e.g. a violated precondition). */
    public void expectThrows(String description, Class<? extends Throwable> expectedType, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("  [FAIL] " + description + " (no exception thrown)");
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                passed++;
                System.out.println("  [PASS] " + description);
            } else {
                failed++;
                System.out.println("  [FAIL] " + description + " (wrong exception type: " + t.getClass() + ")");
            }
        }
    }

    public void summary() {
        System.out.println(suiteName + ": " + passed + " passed, " + failed + " failed"
                + " (" + (passed + failed) + " total)");
        if (failed > 0) {
            // Non-zero exit so this can be wired into a CI step later.
            System.exit(1);
        }
    }
}
