package com.mojtaba.pocketledger.core.security.logging

import org.junit.Assert.assertEquals
import org.junit.Test

class SensitiveValueRedactorTest {
    private val redactor = SensitiveValueRedactor()

    @Test
    fun tokenValuesAreRedacted() {
        assertEquals(
            "token=[REDACTED]",
            redactor.redact("token=abcd1234"),
        )
    }

    @Test
    fun passwordValuesAreRedacted() {
        assertEquals(
            "password=[REDACTED]",
            redactor.redact("password=myPassword"),
        )
    }

    @Test
    fun accessTokenValuesAreRedacted() {
        assertEquals(
            "access_token=[REDACTED]",
            redactor.redact("access_token=XYZ"),
        )
    }

    @Test
    fun merchantValuesAreRedacted() {
        assertEquals(
            "merchant=[REDACTED]",
            redactor.redact("merchant=Starbucks"),
        )
    }

    @Test
    fun noteValuesWithSpacesAreRedacted() {
        assertEquals(
            "note=[REDACTED]",
            redactor.redact("note=Vacation in Vancouver"),
        )
    }

    @Test
    fun multipleValuesAreRedacted() {
        assertEquals(
            "token=[REDACTED] merchant=[REDACTED] status=ok",
            redactor.redact("token=abcd1234 merchant=Starbucks status=ok"),
        )
    }

    @Test
    fun bearerTokensAreRedacted() {
        assertEquals(
            "Authorization: Bearer [REDACTED]",
            redactor.redact("Authorization: Bearer abcd1234"),
        )
    }

    @Test
    fun nullAndEmptyInputReturnEmptyString() {
        assertEquals("", redactor.redact(null))
        assertEquals("", redactor.redact(""))
    }

    @Test
    fun safeOperationalMessagesArePreserved() {
        assertEquals(
            "Screen navigation succeeded screen=Dashboard",
            redactor.redact("Screen navigation succeeded screen=Dashboard"),
        )
    }
}
