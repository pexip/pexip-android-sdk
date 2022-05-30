package com.pexip.sdk.api.infinity

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DtmfRequestTest {

    @Test
    fun `valid digits return a DtmfRequest`() {
        val digits = Random.nextDigits(100)
        val request = DtmfRequest(digits)
        assertEquals(digits, request.digits)
    }

    @Test(IllegalArgumentException::class)
    fun `invalid digits throw`() {
        DtmfRequest(Random.nextString(100))
    }
}
