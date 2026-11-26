package com.toix.core.asset

import com.toix.core.Component

/**
 * Base asset type.
 */
sealed class AssetType {
    object Texture : AssetType()
    object Mesh : AssetType()
    object Audio : AssetType()
    object Material : AssetType()
    object Animation : AssetType()
    object Prefab : AssetType()
    object Level : AssetType()
}

/**
 * Raw asset metadata.
 */
data class AssetMetadata(
    val id: String,
    val path: String,
    val type: AssetType,
    val size: Long,
    val hash: String,
    val importedAt: Long = System.currentTimeMillis(),
)

/**
 * Imported asset result.
 */
data class ImportedAsset(
    val metadata: AssetMetadata,
    val data: ByteArray,
    val dependencies: List<String> = emptyList(),
)

/**
 * Asset importer interface.
 */
interface AssetImporter {
    val supportedExtensions: List<String>

    fun importAsset(path: String): ImportedAsset
}

/**
 * Texture importer (stub - loads raw bytes).
 */
class TextureImporter : AssetImporter {
    override val supportedExtensions = listOf("png", "jpg", "jpeg", "tga", "bmp", "exr")

    override fun importAsset(path: String): ImportedAsset {
        // Stub implementation - real implementation reads file bytes
        return ImportedAsset(
            metadata = AssetMetadata(
                id = path.substringAfterLast("/").substringBeforeLast("."),
                path = path,
                type = AssetType.Texture,
                size = 0,
                hash = "",
            ),
            data = byteArrayOf(),
        )
    }
}

/**
 * Mesh importer (stub - glTF/OBJ).
 */
class MeshImporter : AssetImporter {
    override val supportedExtensions = listOf("gltf", "glb", "obj", "fbx")

    override fun importAsset(path: String): ImportedAsset {
        return ImportedAsset(
            metadata = AssetMetadata(
                id = path.substringAfterLast("/").substringBeforeLast("."),
                path = path,
                type = AssetType.Mesh,
                size = 0,
                hash = "",
            ),
            data = byteArrayOf(),
        )
    }
}

/**
 * Asset cooker: converts imported assets into platform-optimized formats.
 */
interface AssetCooker {
    fun cook(asset: ImportedAsset): ByteArray
}

/**
 * Default asset cooker (pass-through).
 */
class DefaultAssetCooker : AssetCooker {
    override fun cook(asset: ImportedAsset): ByteArray = asset.data
}

/**
 * Asset registry: tracks all known assets and their metadata.
 */
class AssetRegistry {
    private val assets = mutableMapOf<String, AssetMetadata>()

    fun register(metadata: AssetMetadata) {
        assets[metadata.id] = metadata
    }

    fun get(id: String): AssetMetadata? = assets[id]

    fun all(): List<AssetMetadata> = assets.values.toList()
}

/**
 * Cooking pipeline: import -> cook -> store.
 */
class AssetPipeline(
    private val importers: MutableMap<String, AssetImporter> = mutableMapOf(),
    private val cooker: AssetCooker = DefaultAssetCooker(),
    private val registry: AssetRegistry = AssetRegistry(),
) {
    fun registerImporter(extension: String, importer: AssetImporter) {
        importers[extension] = importer
    }

    fun importAndCook(path: String): ImportedAsset? {
        val ext = path.substringAfterLast(".")
        val importer = importers[ext] ?: return null
        val imported = importer.importAsset(path)
        val cooked = cooker.cook(imported)
        registry.register(imported.metadata)
        return imported.copy(data = cooked)
    }

    fun getRegistry(): AssetRegistry = registry
}
