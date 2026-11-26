package com.toix.core.assets

interface Asset {
    val id: String
    fun unload()
}

data class TextureAsset(
    override val id: String,
    val path: String,
    val width: Int,
    val height: Int,
) : Asset {
    override fun unload() {}
}

data class AudioAsset(
    override val id: String,
    val path: String,
    val duration: Float,
) : Asset {
    override fun unload() {}
}

data class PrefabAsset(
    override val id: String,
    val path: String,
) : Asset {
    override fun unload() {}
}

class AssetManager {
    private val cache = mutableMapOf<String, Asset>()
    private val loaders = mutableMapOf<String, AssetLoader>()

    fun registerLoader(extension: String, loader: AssetLoader) {
        loaders[extension] = loader
    }

    fun <T : Asset> load(id: String, path: String): T? {
        if (cache.containsKey(id)) return cache[id] as T?
        val ext = path.substringAfterLast(".")
        val loader = loaders[ext] ?: return null
        val asset = loader.load(id, path)
        cache[id] = asset
        return asset as T?
    }

    fun <T : Asset> get(id: String): T? = cache[id] as T?

    fun unload(id: String) {
        cache[id]?.unload()
        cache.remove(id)
    }
}

interface AssetLoader {
    fun load(id: String, path: String): Asset
}

class TextureLoader : AssetLoader {
    override fun load(id: String, path: String): Asset = TextureAsset(id, path, 512, 512)
}

class AudioLoader : AssetLoader {
    override fun load(id: String, path: String): Asset = AudioAsset(id, path, 10f)
}