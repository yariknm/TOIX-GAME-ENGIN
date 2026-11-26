package com.toix.core.input

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.World

data class InputEvent(
    val type: InputType,
    val value: Float = 0f,
    val isPressed: Boolean = false,
    val x: Float = 0f,
    val y: Float = 0f,
    val time: Float = 0f,
)

enum class InputType {
    MOUSE_MOVE,
    MOUSE_BUTTON_DOWN,
    MOUSE_BUTTON_UP,
    KEY_PRESS,
    KEY_RELEASE,
    TOUCH_BEGIN,
    TOUCH_END,
    TOUCH_MOVE,
}

class InputManager {
    private val listeners = mutableListOf<(InputEvent) -> Unit>()

    fun addListener(listener: (InputEvent) -> Unit) {
        listeners += listener
    }

    fun dispatch(event: InputEvent) {
        for (listener in listeners) {
            listener(event)
        }
    }

    fun mouseMove(x: Float, y: Float) {
        dispatch(InputEvent(InputType.MOUSE_MOVE, x = x, y = y))
    }

    fun mouseButton(down: Boolean, x: Float, y: Float) {
        dispatch(InputEvent(if (down) InputType.MOUSE_BUTTON_DOWN else InputType.MOUSE_BUTTON_UP, isPressed = down, x = x, y = y))
    }

    fun keyPress() {
        dispatch(InputEvent(InputType.KEY_PRESS, isPressed = true))
    }

    fun keyRelease() {
        dispatch(InputEvent(InputType.KEY_RELEASE, isPressed = false))
    }

    fun touchBegin(x: Float, y: Float) {
        dispatch(InputEvent(InputType.TOUCH_BEGIN, isPressed = true, x = x, y = y))
    }

    fun touchEnd(x: Float, y: Float) {
        dispatch(InputEvent(InputType.TOUCH_END, isPressed = false, x = x, y = y))
    }
}

class InputComponent(
    val inputManager: InputManager,
    var enabled: Boolean = true,
) : Component {
    override fun snapshot(): Component = InputComponent(inputManager, enabled)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}