package com.pexip.sdk.sample

import com.pexip.sdk.sample.alias.AliasWorkflow
import com.pexip.sdk.sample.conference.ConferenceWorkflow
import com.pexip.sdk.sample.node.NodeWorkflow
import com.pexip.sdk.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.sample.pinrequirement.PinRequirementWorkflow

object WorkflowComponent {

    private val aliasWorkflow by lazy { AliasWorkflow() }
    private val nodeWorkflow by lazy {
        NodeWorkflow(NetworkComponent.nodeResolver, NetworkComponent.service)
    }
    private val pinRequirementWorkflow by lazy { PinRequirementWorkflow(NetworkComponent.service) }
    private val pinChallengeWorkflow by lazy { PinChallengeWorkflow(NetworkComponent.service) }
    private val conferenceWorkflow by lazy {
        ConferenceWorkflow(
            service = NetworkComponent.service,
            factory = MediaConnectionComponent.factory
        )
    }

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
