package com.mojtaba.folentra.core.security.logging

class SensitiveValueRedactor {
    fun redact(input: String?): String {
        if (input.isNullOrEmpty()) return input.orEmpty()

        return redactionRules.fold(input) { current, rule ->
            rule.redact(current)
        }
    }

    private companion object {
        private const val REDACTED = "[REDACTED]"

        private val sensitiveKeys = listOf(
            "access_token",
            "account",
            "account_id",
            "account_identifier",
            "amount",
            "amount_minor",
            "budget",
            "budget_amount",
            "category",
            "credential",
            "credential_id",
            "encrypted_payload",
            "encryption_key",
            "key",
            "merchant",
            "note",
            "passkey",
            "password",
            "query",
            "search",
            "secret",
            "session",
            "session_token",
            "tag",
            "token",
        ).joinToString("|") { Regex.escape(it) }

        private val keyValueRule = Regex(
            pattern = """\b($sensitiveKeys)\b(\s*[:=]\s*)("[^"]*"|'[^']*'|.*?)(?=(?:\s+\b[\w.-]+\b\s*[:=])|[,;\r\n]|$)""",
            options = setOf(RegexOption.IGNORE_CASE),
        )

        private val bearerRule = Regex(
            pattern = """\bBearer\s+[A-Za-z0-9._~+/=-]+""",
            options = setOf(RegexOption.IGNORE_CASE),
        )

        private val authorizationHeaderRule = Regex(
            pattern = """\bAuthorization(\s*[:=]\s*)(?:(Bearer|Basic)\s+)?[A-Za-z0-9._~+/=-]+""",
            options = setOf(RegexOption.IGNORE_CASE),
        )

        private val jwtRule = Regex(
            pattern = """\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\b""",
        )

        private val redactionRules = listOf(
            RedactionRule(authorizationHeaderRule) { match ->
                val scheme = match.groupValues[2].takeIf { it.isNotBlank() }
                "Authorization${match.groupValues[1]}${scheme?.plus(" ") ?: ""}$REDACTED"
            },
            RedactionRule(keyValueRule) { match ->
                "${match.groupValues[1]}${match.groupValues[2]}$REDACTED"
            },
            RedactionRule(bearerRule) { "Bearer $REDACTED" },
            RedactionRule(jwtRule) { REDACTED },
        )
    }
}

private class RedactionRule(
    private val regex: Regex,
    private val replacement: (MatchResult) -> String,
) {
    fun redact(value: String): String = regex.replace(value, replacement)
}
