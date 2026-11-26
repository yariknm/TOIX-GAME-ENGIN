package com.toix.core.serialization

import com.toix.core.Actor
import com.toix.core.BlueprintComponent
import com.toix.core.BlueprintNode
import com.toix.core.Component
import com.toix.core.LogPositionNode
import com.toix.core.MoveByNode
import com.toix.core.TransformComponent
import com.toix.core.World
import com.toix.core.get
import com.toix.core.render.Color
import com.toix.core.render.SpriteRendererComponent

data class SerializableWorld(
    val name: String,
    val actors: List<SerializableActor>,
)

data class SerializableActor(
    val id: String,
    val name: String,
    val transform: SerializableTransform,
    val components: List<SerializableComponent>,
)

data class SerializableTransform(
    val x: Float,
    val y: Float,
    val z: Float,
    val scaleX: Float,
    val scaleY: Float,
)

data class SerializableSpriteRenderer(
    val spriteName: String,
    val width: Float,
    val height: Float,
    val r: Float,
    val g: Float,
    val b: Float,
    val a: Float,
    val visible: Boolean,
    val layer: Int,
)

sealed class SerializableComponent {
    data class SpriteRenderer(
        val spriteName: String,
        val width: Float,
        val height: Float,
        val r: Float,
        val g: Float,
        val b: Float,
        val a: Float,
        val visible: Boolean,
        val layer: Int,
    ) : SerializableComponent()

    data class Blueprint(
        val entryNodeId: String?,
        val nodes: List<SerializableBlueprintNode>,
    ) : SerializableComponent()
}

sealed class SerializableBlueprintNode {
    data class MoveByNode(
        val id: String,
        val vx: Float,
        val vy: Float,
        val nextNodeId: String?,
    ) : SerializableBlueprintNode()

    data class LogPositionNode(
        val id: String,
        val tag: String,
        val nextNodeId: String?,
    ) : SerializableBlueprintNode()
}

object WorldSerializer {

    /** Минимальный JSON-парсер без внешних зависимостей. */
    class JsonParser(private val input: String) {
        private var pos = 0

        fun parse(): Any? {
            skipWhitespace()
            return parseValue()
        }

        private fun parseValue(): Any? {
            skipWhitespace()
            return when (peek()) {
                '"' -> parseString()
                '-', in '0'..'9' -> parseNumber()
                't', 'f' -> parseBoolean()
                '[' -> parseArray()
                '{' -> parseObject()
                'n' -> parseNull()
                else -> throw IllegalArgumentException("Unexpected: ${peek()} at $pos")
            }
        }

        private fun parseObject(): MutableMap<String, Any?> {
            expect('{')
            val map = mutableMapOf<String, Any?>()
            while (true) {
                skipWhitespace()
                if (peek() == '}') { pos++; break }
                val key = parseString()
                skipWhitespace()
                expect(':')
                map[key] = parseValue()
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    '}' -> { pos++; break }
                    else -> throw IllegalArgumentException("Expected , or } at $pos")
                }
            }
            return map
        }

        private fun parseArray(): MutableList<Any?> {
            expect('[')
            val list = mutableListOf<Any?>()
            while (true) {
                skipWhitespace()
                if (peek() == ']') { pos++; break }
                list.add(parseValue())
                skipWhitespace()
                when (peek()) {
                    ',' -> pos++
                    ']' -> { pos++; break }
                    else -> throw IllegalArgumentException("Expected , or ] at $pos")
                }
            }
            return list
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                val c = input[pos++]
                if (c == '\\') {
                    val next = input[pos++]
                    sb.append(when (next) {
                        '"' -> '"'
                        '\\' -> '\\'
                        '/' -> '/'
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        else -> next
                    })
                } else if (c == '"') break
                else sb.append(c)
            }
            return sb.toString()
        }

        private fun parseNumber(): Any {
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] in ".-+eE")) pos++
            val s = input.substring(start, pos)
            return if (s.contains('.') || s.contains('e') || s.contains('E')) s.toDouble() else s.toInt()
        }

        private fun parseBoolean(): Any {
            if (input.startsWith("true", pos)) { pos += 4; return true }
            if (input.startsWith("false", pos)) { pos += 5; return false }
            throw IllegalArgumentException("Expected boolean at $pos")
        }

        private fun parseNull(): Any? {
            if (input.startsWith("null", pos)) { pos += 4; return null }
            throw IllegalArgumentException("Expected null at $pos")
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }
        private fun peek(): Char = if (pos < input.length) input[pos] else '\u0000'
        private fun expect(c: Char) { if (peek() == c) pos++ else throw IllegalArgumentException("Expected $c at $pos") }
    }

    fun serialize(world: World): String {
        val sb = StringBuilder()
        sb.append("{\"name\":").append(jsonString(world.name)).append(",\"actors\":[")
        val actors = world.allActors().map { serializeActor(it) }
        sb.append(actors.joinToString(","))
        sb.append("]}")
        return sb.toString()
    }

    fun deserialize(json: String): World {
        val parsed = JsonParser(json).parse()
        @Suppress("UNCHECKED_CAST")
        val map = parsed as Map<String, Any?>
        val name = map["name"]?.toString() ?: "Level_01"
        val world = World(name)
        @Suppress("UNCHECKED_CAST")
        val actors = map["actors"] as? List<Map<String, Any?>>
        if (actors != null) {
            for (a in actors) {
                world.spawn(deserializeActor(a))
            }
        }
        return world
    }

    private fun serializeActor(actor: Actor): String {
        val tr = actor.get<TransformComponent>()?.transform ?: com.toix.core.Transform()
        val t = jsonMapOf(
            "x" to tr.x, "y" to tr.y, "z" to tr.z,
            "scaleX" to tr.scaleX, "scaleY" to tr.scaleY
        )
        val comps = actor.allComponents().mapNotNull { serializeComponent(it) }.joinToString(",")
        return jsonMapOf(
            "id" to actor.id,
            "name" to actor.name,
            "transform" to t,
            "components" to "[${comps}]"
        )
    }

    private fun deserializeActor(data: Map<String, Any?>): Actor {
        val id = data["id"]?.toString() ?: java.util.UUID.randomUUID().toString()
        val name = data["name"]?.toString() ?: "Actor"
        val actor = Actor(id, name)
        val t = data["transform"] as? Map<String, Any?>
        if (t != null) {
            val tr = actor.transform
            tr.x = (t["x"] as? Number)?.toFloat() ?: 0f
            tr.y = (t["y"] as? Number)?.toFloat() ?: 0f
            tr.z = (t["z"] as? Number)?.toFloat() ?: 0f
            tr.scaleX = (t["scaleX"] as? Number)?.toFloat() ?: 1f
            tr.scaleY = (t["scaleY"] as? Number)?.toFloat() ?: 1f
        }
        @Suppress("UNCHECKED_CAST")
        val comps = data["components"] as? List<Map<String, Any?>> ?: emptyList()
        for (c in comps) {
            deserializeComponent(actor, c)
        }
        return actor
    }

    private fun deserializeComponent(actor: Actor, data: Map<String, Any?>) {
        val type = data["type"]?.toString()
        when (type) {
            "SpriteRenderer" -> {
                val c = data
                actor.add(SpriteRendererComponent(
                    c["spriteName"]?.toString() ?: "",
                    (c["width"] as? Number)?.toFloat() ?: 1f,
                    (c["height"] as? Number)?.toFloat() ?: 1f,
                    Color(
                        (c["r"] as? Number)?.toFloat() ?: 1f,
                        (c["g"] as? Number)?.toFloat() ?: 1f,
                        (c["b"] as? Number)?.toFloat() ?: 1f,
                        (c["a"] as? Number)?.toFloat() ?: 1f,
                    ),
                    c["visible"] as? Boolean ?: true,
                    c["layer"] as? Int ?: 0,
                ))
            }
            "Blueprint" -> {
                val entryId = data["entryNodeId"]?.toString()
                @Suppress("UNCHECKED_CAST")
                val nodes = data["nodes"] as? List<Map<String, Any?>> ?: emptyList()
                val nodesById = mutableMapOf<String, BlueprintNode>()
                for (n in nodes) {
                    val node = deserializeBlueprintNode(n)
                    nodesById[node.id] = node
                }
                actor.add(BlueprintComponent(entryId, nodesById))
            }
        }
    }

    private fun deserializeBlueprintNode(data: Map<String, Any?>): BlueprintNode {
        val type = data["type"]?.toString()
        return when (type) {
            "MoveByNode" -> MoveByNode(
                id = data["id"]?.toString() ?: "",
                vx = (data["vx"] as? Number)?.toFloat() ?: 0f,
                vy = (data["vy"] as? Number)?.toFloat() ?: 0f,
                nextNodeId = data["nextNodeId"]?.toString()
            )
            "LogPositionNode" -> LogPositionNode(
                id = data["id"]?.toString() ?: "",
                tag = data["tag"]?.toString() ?: "",
                nextNodeId = data["nextNodeId"]?.toString()
            )
            else -> MoveByNode("", 0f, 0f, null)
        }
    }

    private fun serializeComponent(component: Component): String? {
        return when (component) {
            is SpriteRendererComponent -> jsonMapOf(
                "type" to "SpriteRenderer",
                "spriteName" to component.spriteName,
                "width" to component.width,
                "height" to component.height,
                "r" to component.color.r,
                "g" to component.color.g,
                "b" to component.color.b,
                "a" to component.color.a,
                "visible" to component.visible,
                "layer" to component.layer
            )
            is BlueprintComponent -> {
                val nodes = component.nodesById.values.map { serializeBlueprintNode(it) }.joinToString(",")
                jsonMapOf("type" to "Blueprint", "entryNodeId" to component.entryNodeId, "nodes" to "[${nodes}]")
            }
            else -> null
        }
    }

    private fun serializeBlueprintNode(node: BlueprintNode): String {
        return when (node) {
            is MoveByNode -> jsonMapOf(
                "type" to "MoveByNode",
                "id" to node.id,
                "vx" to node.vx,
                "vy" to node.vy,
                "nextNodeId" to node.nextNodeId
            )
            is LogPositionNode -> jsonMapOf(
                "type" to "LogPositionNode",
                "id" to node.id,
                "tag" to node.tag,
                "nextNodeId" to node.nextNodeId
            )
            else -> throw IllegalArgumentException("Unknown BlueprintNode")
        }
    }

    private fun jsonMapOf(vararg pairs: Pair<String, Any?>): String {
        val items = pairs.map { "\"${it.first}\":${jsonValue(it.second)}" }.toList()
        return "{" + items.joinToString(", ") + "}"
    }

    private fun jsonString(s: String): String {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }

    private fun jsonValue(v: Any?): String = when (v) {
        null -> "null"
        is String -> jsonString(v)
        is Number -> v.toString()
        is Boolean -> v.toString()
        else -> throw IllegalArgumentException("Unsupported: ${v?.javaClass}")
    }
}