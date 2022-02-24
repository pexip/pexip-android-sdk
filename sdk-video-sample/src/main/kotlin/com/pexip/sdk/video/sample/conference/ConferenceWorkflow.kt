package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.Conference
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import kotlinx.coroutines.awaitCancellation
import okhttp3.OkHttpClient

class ConferenceWorkflow(private val client: OkHttpClient) :
    StatefulWorkflow<ConferenceProps, Conference, ConferenceOutput, ConferenceRendering>() {

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): Conference =
        Conference.Builder()
            .token(props.token)
            .client(client)
            .build()

    override fun snapshotState(state: Conference): Snapshot? = null

    override fun render(
        renderProps: ConferenceProps,
        renderState: Conference,
        context: RenderContext,
    ): ConferenceRendering {
        context.runningSideEffect(renderState.toString()) {
            try {
                awaitCancellation()
            } finally {
                renderState.leave()
            }
        }
        return ConferenceRendering(onBackClick = context.send(::OnBackClick))
    }
}
