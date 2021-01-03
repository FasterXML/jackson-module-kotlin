# Failing tests

These are tests for filed issues on GitHub that have not been fixed.

The tests pass because either the failing assertions have their `AssertionError` or the Jackson call
that throws has its exception caught.  Each of those blocks has a `fail()` call after the call that
is expected to throw; this `fail()` will fail the build if the associated test case is fixed,
allowing us to know when an issue has been solved incidentally.

## Writing a failing test

Failing tests are good to have because they can both concisely document known issues with the
library but also serve as notification for when that functionality has been fixed.

Create a failing test by writing it as you would any other test, making any calls or assertions
necessary to demonstrate the failing behavior.  Then, suppress that error using a `try/catch`
block with a descriptive `fail()` statement as the last line of the `try` to act as a
[canary](https://en.wikipedia.org/wiki/Domestic_canary#Miner's_canary) for when the broken
functionality is fixed.

See the below examples:

```kotlin
@Test
fun failingTest() {
    try {
        mapper.callThatFailsWithMismatchedInputException()
        fail("The call that fails with MismatchedInputException has been fixed!")
    } catch (e: MismatchedInputException) {
        // Remove this try/catch and the `fail()` call above when this issue is fixed
    }
}
```

```kotlin
@Test
fun serializeAndDeserializeTypeable() {
    val oldEntity = MyEntity(null, null)
    val json = mapper.writeValueAsString(oldEntity)
    val newEntity = mapper.readValue<MyEntity>(json)

    try {
        // newEntity.type is hte string "null" instead of the null value
        assertNull(newEntity.type)
        fail("GitHub #335 has been fixed!")
    } catch (e: AssertionError) {
        // Remove this try/catch and the `fail()` call above when this issue is fixed
    }
}
```
