package com.toix.core.material

import com.toix.core.Component
import com.toix.core.Actor
import com.toix.core.World
import com.toix.core.render.Color

/**
 * Shader program description: vertex + fragment sources or SPIR-V bytecode.
 */
data class Shader(
    val id: String,
    val vertexSource: String,
    val fragmentSource: String,
    val defines: Map<String, String> = emptyMap(),
)

/**
 * Material parameter value.
 */
sealed class MaterialParameterValue {
    data class FloatVal(val value: Float) : MaterialParameterValue()
    data class ColorVal(val r: Float, val g: Float, val b: Float, val a: Float) : MaterialParameterValue()
    data class Vector2Val(val x: Float, val y: Float) : MaterialParameterValue()
    data class Vector3Val(val x: Float, val y: Float, val z: Float) : MaterialParameterValue()
    data class TextureVal(val textureId: String) : MaterialParameterValue()
}

/**
 * Material instance with parameter overrides.
 */
data class Material(
    val id: String,
    val shader: Shader,
    val parameters: Map<String, MaterialParameterValue> = emptyMap(),
    var renderQueue: Int = 0,
    var twoSided: Boolean = false,
    var blendMode: BlendMode = BlendMode.Opaque,
) {
    companion object {
        fun createUnlit(color: Color): Material {
            return Material(
                id = "unlit_${System.currentTimeMillis()}",
                shader = Shader(
                    id = "unlit",
                    vertexSource = "",
                    fragmentSource = "",
                ),
                parameters = mapOf(
                    "_Color" to MaterialParameterValue.ColorVal(color.r, color.g, color.b, color.a),
                ),
                blendMode = BlendMode.Opaque,
            )
        }
    }
}

/**
 * Blend modes for material rendering.
 */
enum class BlendMode {
    Opaque,
    Masked,
    Transparent,
    Additive,
    Multiply,
}

/**
 * Material component attached to an actor for rendering.
 */
class MaterialComponent(
    var material: Material?,
) : Component {
    override fun snapshot(): Component = MaterialComponent(material)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}
