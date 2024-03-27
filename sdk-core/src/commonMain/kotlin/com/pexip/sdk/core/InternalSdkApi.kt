/*
 * Copyright 2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.core

/**
 * Marks declarations that are **internal** in SDK API, which means that should not be used
 * outside of `com.pexip.sdk`, because their signatures and semantics will change between future
 * releases without any warnings and without providing any migration aids.
 */
@Retention(value = AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.PROPERTY,
)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = """
        This is an internal com.pexip.sdk API that should not be used from outside of com.pexip.sdk. 
        No compatibilivvty guarantees are provided. It is recommended to report your use-case of 
        internal API to com.pexip.sdk issue tracker, so stable API could be provided instead.
    """,
)
public annotation class InternalSdkApi
