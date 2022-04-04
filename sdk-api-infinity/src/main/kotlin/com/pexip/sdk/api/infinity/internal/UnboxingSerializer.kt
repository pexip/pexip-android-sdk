package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

internal abstract class UnboxingSerializer<T : Any>(tSerializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(tSerializer) {

    final override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject.getValue("result")
}
