/*
 * Copyright 2023 Pexip AS
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
package com.pexip.sdk.media.webrtc.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.pexip.sdk.media.SecureCheckCode
import kotlin.test.Test

internal class SecureCheckCodeTest {

    @Test
    fun `returns correct value`() {
        tableOf("localFingerprints", "remoteFingerprints", "secureCheckCode")
            .row<List<String>, List<String>, String?>(
                val1 = emptyList(),
                val2 = emptyList(),
                val3 = null,
            )
            .row(
                val1 = List(10, Int::toString),
                val2 = emptyList(),
                val3 = null,
            )
            .row(
                val1 = emptyList(),
                val2 = List(10, Int::toString),
                val3 = null,
            )
            .row(
                val1 = listOf(
                    "3d28271ec52e3d07fe14f5f16d01f2c09cbcac1949f9904b305136d0edbee12d",
                    "f03247d1c40e0b1d76eba9a48256e1ab1a557a24a592dcc42570bc075d529e4c",
                ),
                val2 = listOf(
                    "c625ca57418821d8e717df1b71bf589a042d8fc0f0a2c3776090e155d2d377d3",
                    "beb5f267e132caaf8e303a4281fcdf78e7d2ecf13551dd9027142f3aba050fa0",
                ),
                val3 = "b867a2458e310a536d5535e4c0feef6e77d178d910db1d62e66e363e361f07da",
            )
            .row(
                val1 = listOf(
                    "beb5f267e132caaf8e303a4281fcdf78e7d2ecf13551dd9027142f3aba050fa0",
                    "c625ca57418821d8e717df1b71bf589a042d8fc0f0a2c3776090e155d2d377d3",
                ),
                val2 = listOf(
                    "f03247d1c40e0b1d76eba9a48256e1ab1a557a24a592dcc42570bc075d529e4c",
                    "3d28271ec52e3d07fe14f5f16d01f2c09cbcac1949f9904b305136d0edbee12d",
                ),
                val3 = "b867a2458e310a536d5535e4c0feef6e77d178d910db1d62e66e363e361f07da",
            )
            .row(
                val1 = listOf(
                    "ca66a852a9e96c40f4cce7972d994914909b646b2564e8d25dd4003656b3dd63",
                    "639897ea9580738d876682a1a57d91b36a469119b59174caf441b6e1a41047b3",
                ),
                val2 = listOf(
                    "55afd11eaeda11a6309cd3d93c69f62b1a0f4c489c6f0daa42daf6d28f1ded39",
                    "3d6977e146853baef774f23c69fc020d07723efd9bb68d18a3c593a831d538e6",
                ),
                val3 = "49a63af80decab3254aec21b2143e11d52cfacabded771b4c3b307d3487fdf54",
            )
            .row(
                val1 = listOf(
                    "3d6977e146853baef774f23c69fc020d07723efd9bb68d18a3c593a831d538e6",
                    "55afd11eaeda11a6309cd3d93c69f62b1a0f4c489c6f0daa42daf6d28f1ded39",
                ),
                val2 = listOf(
                    "639897ea9580738d876682a1a57d91b36a469119b59174caf441b6e1a41047b3",
                    "ca66a852a9e96c40f4cce7972d994914909b646b2564e8d25dd4003656b3dd63",
                ),
                val3 = "49a63af80decab3254aec21b2143e11d52cfacabded771b4c3b307d3487fdf54",
            )
            .forAll { localFingerprints, remoteFingerprints, expected ->
                val actual = SecureCheckCode(localFingerprints, remoteFingerprints)
                assertThat(actual).isEqualTo(expected?.let(::SecureCheckCode))
            }
    }
}
