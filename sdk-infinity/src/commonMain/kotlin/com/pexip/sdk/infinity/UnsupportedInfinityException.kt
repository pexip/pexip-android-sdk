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
package com.pexip.sdk.infinity

/**
 * Thrown to indicate that the Infinity deployment is not supported
 *
 * @property versionId a version of Infinity that caused this exception
 */
public class UnsupportedInfinityException(public val versionId: VersionId) : IllegalArgumentException("Infinity $versionId is not supported by the SDK. Please upgrade your Infinity deployment to 29 or newer.") {

    /**
     * A version of Infinity that caused this exception
     */
    @Deprecated(
        message = "Use versionId instead.",
        replaceWith = ReplaceWith(expression = "versionId.value"),
        level = DeprecationLevel.ERROR,
    )
    public val version: String get() = versionId.value
}
