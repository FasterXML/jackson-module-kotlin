package com.fasterxml.jackson.module.kotlin.test

import kotlin.reflect.KClass
import kotlin.test.fail

/**
 * Expect a block to throw an exception.  If a different type of exception is thrown or no
 * exception is produced by the block, fail the test.  In the latter case, no exception being
 * thrown, fixMessage is printed.
 *
 * This function is intended to allow failing tests to be written and run as part of the build
 * without causing it to fail, except if the failure is fixed, in which case the fixMessage
 * should make it clear what has happened (i.e. that some previously broken functionality
 * has been fixed).
 *
 * @param throwableType The expected throwable
 * @param fixMessage    The message to print when the block succeeds
 * @param block         The block to execute
 */
fun <T : Throwable> expectFailure(
        throwableType: KClass<T>,
        fixMessage: String,
        block: () -> Unit
) {
    try {
        block()
    } catch (e: Throwable) {
        if (throwableType.isInstance(e)) {
            // Expected exception, do nothing
        } else {
            fail("Expected $throwableType but got $e")
        }

        return
    }

    fail(fixMessage)
}
