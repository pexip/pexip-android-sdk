/*
 * Copyright 2022-2025 Pexip AS
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
package com.pexip.sdk.sample.bandwidth

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import javax.inject.Inject
import javax.inject.Singleton

private typealias BandwidthRenderContext =
    StatefulWorkflow.RenderContext<BandwidthProps, BandwidthState, BandwidthOutput>

@Singleton
class BandwidthWorkflow @Inject constructor() :
    StatefulWorkflow<BandwidthProps, BandwidthState, BandwidthOutput, BandwidthScreen>() {

    override fun initialState(props: BandwidthProps, snapshot: Snapshot?): BandwidthState =
        BandwidthState()

    override fun snapshotState(state: BandwidthState): Snapshot? = null

    override fun render(
        renderProps: BandwidthProps,
        renderState: BandwidthState,
        context: BandwidthRenderContext,
    ) = BandwidthScreen(
        visible = renderProps.visible,
        bandwidth = renderState.bandwidth,
        onBandwidthClick = context.send(::onBandwidthClick),
        onBackClick = context.send(::onBackClick),
    )

    private fun onBandwidthClick(bandwidth: Bandwidth) =
        action({ "onBandwidthClick($bandwidth)" }) {
            state = BandwidthState(bandwidth = bandwidth)
            setOutput(BandwidthOutput.ChangeBandwidth(bandwidth = bandwidth))
        }

    private fun onBackClick() = action({ "onBackClick()" }) {
        setOutput(BandwidthOutput.Back)
    }
}
