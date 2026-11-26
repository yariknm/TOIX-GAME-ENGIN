package com.toix.core.render

class MockRenderBackend : RenderBackend {
    val drawCalls = mutableListOf<String>()

    override fun clear(color: Color) {
        drawCalls.add("CLEAR ${color.r},${color.g},${color.b},${color.a}")
    }

    override fun draw(command: DrawCommand) {
        drawCalls.add("DRAW ${command.spriteName} at (${command.x},${command.y}) w=${command.width} h=${command.height} layer=${command.layer}")
    }

    override fun present() {
        drawCalls.add("PRESENT")
    }

    override fun setViewport(width: Float, height: Float) {
        drawCalls.add("VIEWPORT ${width}x${height}")
    }
}