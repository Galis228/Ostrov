package com.pixelisland.core

/** Небольшой кеш с вытеснением самых старых записей — чтобы ничего не копилось бесконечно. */
class Lru<K, V>(private val max: Int) : LinkedHashMap<K, V>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?) = size > max
}
