# Failing tests

These are tests for filed issues on GitHub that have not been fixed.

The tests pass because either the failing assertions have their `AssertionError` or the Jackson call
that throws has its exception caught.  Each of those blocks has a `fail()` call after the call that
is expected to throw; this `fail()` will fail the build if the associated test case is fixed,
allowing us to know when an issue has been solved incidentally.
