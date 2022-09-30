package com.pexip.sdk.sample.permissions

import android.content.Context
import com.pexip.sdk.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionsWorkflow @Inject constructor(@ApplicationContext private val context: Context) :
    StatefulWorkflow<PermissionsProps, PermissionsState, PermissionsOutput, PermissionsRendering>() {

    override fun initialState(props: PermissionsProps, snapshot: Snapshot?): PermissionsState =
        PermissionsState(props.permissions.associateWith { false })

    override fun snapshotState(state: PermissionsState): Snapshot? = null

    override fun render(
        renderProps: PermissionsProps,
        renderState: PermissionsState,
        context: RenderContext,
    ): PermissionsRendering = PermissionsRendering(
        permissions = renderProps.permissions,
        onPermissionsRequestResult = context.send(::OnPermissionsRequestResult),
        onBackClick = context.send(::OnBackClick)
    )
}
