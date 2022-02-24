package com.pexip.sdk.video.sample

import com.pexip.sdk.video.sample.alias.AliasWorkflow
import com.pexip.sdk.video.sample.conference.ConferenceWorkflow
import com.pexip.sdk.video.sample.node.NodeWorkflow
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementWorkflow

object WorkflowComponent {

    private val aliasWorkflow by lazy { AliasWorkflow() }
    private val nodeWorkflow by lazy { NodeWorkflow(NetworkComponent.nodeResolver) }
    private val pinRequirementWorkflow by lazy {
        PinRequirementWorkflow(NetworkComponent.tokenRequester)
    }
    private val pinChallengeWorkflow by lazy {
        PinChallengeWorkflow(NetworkComponent.tokenRequester)
    }
    private val conferenceWorkflow by lazy { ConferenceWorkflow(NetworkComponent.client) }

    val sampleWorkflow by lazy {
        SampleWorkflow(
            aliasWorkflow = aliasWorkflow,
            nodeWorkflow = nodeWorkflow,
            pinRequirementWorkflow = pinRequirementWorkflow,
            pinChallengeWorkflow = pinChallengeWorkflow,
            conferenceWorkflow = conferenceWorkflow
        )
    }
}
