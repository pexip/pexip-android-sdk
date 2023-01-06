package com.pexip.sdk.sample.permissions

import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import javax.inject.Inject

class PermissionsWorkflow @Inject constructor() :
    StatefulWorkflow<PermissionsProps, PermissionsState, PermissionsOutput, PermissionsRendering>() {

    override fun initialState(props: PermissionsProps, snapshot: Snapshot?): PermissionsState =
        PermissionsState()

    override fun snapshotState(state: PermissionsState): Snapshot? = null

    override fun render(
        renderProps: PermissionsProps,
        renderState: PermissionsState,
        context: RenderContext,
    ): PermissionsRendering = PermissionsRendering(
        permissions = renderProps.permissions,
        onPermissionsRequestResult = context.send(::OnPermissionsRequestResult),
        onBackClick = context.send(::OnBackClick),
    )
}
