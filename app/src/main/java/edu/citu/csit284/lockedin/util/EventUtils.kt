package edu.citu.csit284.lockedin.util

object EventBus {
    private val listeners = mutableMapOf<String, MutableList<(Any?) -> Unit>>()

    fun subscribe(eventType: String, listener: (Any?) -> Unit) {
        if (!listeners.containsKey(eventType)) {
            listeners[eventType] = mutableListOf()
        }
        listeners[eventType]?.add(listener)
    }

    fun unsubscribe(eventType: String, listener: (Any?) -> Unit) {
        listeners[eventType]?.remove(listener)
    }

    fun post(eventType: String, data: Any? = null) {
        listeners[eventType]?.forEach { it(data) }
    }
}