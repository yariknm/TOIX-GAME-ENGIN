package com.toix.core.animation.statemachine

import com.toix.core.Component
import com.toix.core.Actor
import com.toix.core.World

/**
 * State in an animation state machine.
 */
data class AnimationState(
    val name: String,
    val clipName: String?,
    val speed: Float = 1.0f,
    val loop: Boolean = true,
    val entryTime: Float = 0.0f,
)

/**
 * Transition between states with a condition.
 */
data class AnimationTransition(
    val fromState: String,
    val toState: String,
    val condition: (Float) -> Boolean, // normalized time -> should transition
    val duration: Float = 0.2f, // crossfade duration
    val offset: Float = 0.0f,
)

/**
 * Animation state machine: drives Animator based on states and transitions.
 */
class AnimationStateMachine(
    var states: Map<String, AnimationState> = emptyMap(),
    var transitions: List<AnimationTransition> = emptyList(),
    var currentState: String? = null,
    var stateTime: Float = 0.0f,
    var crossfadeTime: Float = 0.0f,
    var previousState: String? = null,
) : Component {
    override fun snapshot(): Component = AnimationStateMachine(
        states, transitions, currentState, stateTime, crossfadeTime, previousState
    )

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {
        if (currentState == null) return

        stateTime += dt

        // Handle crossfade
        if (crossfadeTime > 0.0f) {
            crossfadeTime = maxOf(0.0f, crossfadeTime - dt)
        }

        // Check transitions
        val current = states[currentState]!!
        val normalizedTime = (stateTime % (1.0f / current.speed)) * current.speed // simplified

        for (transition in transitions) {
            if (transition.fromState == currentState && transition.condition(normalizedTime)) {
                previousState = currentState
                currentState = transition.toState
                stateTime = transition.offset
                crossfadeTime = transition.duration
                break
            }
        }
    }

    fun addState(state: AnimationState) {
        states = states.toMutableMap().apply { put(state.name, state) }
    }

    fun addTransition(transition: AnimationTransition) {
        transitions = transitions.toMutableList().apply { add(transition) }
    }

    fun enterState(stateName: String) {
        if (states.containsKey(stateName)) {
            previousState = currentState
            currentState = stateName
            stateTime = 0.0f
            crossfadeTime = 0.0f
        }
    }

    fun getCurrentState(): AnimationState? = currentState?.let { states[it] }
}

/**
 * Component wrapper for attaching state machine to an actor.
 */
class AnimatorStateMachineComponent(
    var stateMachine: AnimationStateMachine,
) : Component {
    override fun snapshot(): Component = AnimatorStateMachineComponent(stateMachine)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {
        stateMachine.onTick(actor, world, dt)
    }
}
