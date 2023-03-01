/*
 * Copyright 2022-2023 Pexip AS
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

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandwidthWorkflow @Inject constructor() :
    StatefulWorkflow<BandwidthProps, BandwidthState, BandwidthOutput, BandwidthRendering>() {

    override fun initialState(props: BandwidthProps, snapshot: Snapshot?): BandwidthState =
        BandwidthState()

    override fun snapshotState(state: BandwidthState): Snapshot? = null

    override fun render(
        renderProps: BandwidthProps,
        renderState: BandwidthState,
        context: RenderContext,
    ) = BandwidthRendering(
        visible = renderProps.visible,
        bandwidth = renderState.bandwidth,
        onBandwidthClick = { context.actionSink.send(OnBandwidthClick(it)) },
        onBackClick = { context.actionSink.send(OnBackClick) },
    )
}
