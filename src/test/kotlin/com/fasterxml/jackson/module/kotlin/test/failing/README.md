# Failing tests

These are tests for filed issues on GitHub that have not been fixed. Failing tests are good to
have because they can both concisely document known issues with the library but also serve as
notification for when that functionality has been fixed.

The tests pass because either the failing assertions have their `AssertionError` or the Jackson call
that throws has its exception caught.

## Writing a failing test

Create a failing test by writing it as you would any other test, making any calls or assertions
necessary to demonstrate the failing behavior.  Then, surround the failing call with the
`expectFailure()` function, passing the expected exception and a message that will be printed
if the failure does _not_ occur, which implies that the functionality has been fixed.

See the below examples:

```kotlin
@Test
fun failingTest() {
    expectFailure(MismatchedInputException::class, "The call that fails with MismatchedInputException has been fixed!") {
        mapper.callThatFailsWithMismatchedInputException()
    }
}
```

```kotlin
@Test
fun serializeAndDeserializeTypeable() {
    val oldEntity = MyEntity(null, null)
    val json = mapper.writeValueAsString(oldEntity)
    val newEntity = mapper.readValue<MyEntity>(json)

    expectFailure(AssertionError::class, "GitHub #335 has been fixed!") {
        // newEntity.type is hte string "null" instead of the null value
        assertNull(newEntity.type)
    }
}
```
