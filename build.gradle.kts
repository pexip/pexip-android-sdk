@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.spotless)
}

spotless {
    kotlinGradle {
        ktlint()
    }
    format("misc") {
        target(".gitignore", "*.md")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}
