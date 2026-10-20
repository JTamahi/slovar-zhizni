package com.example.slovarzhizni

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object QuoteStore {

    private const val TAG = "QuoteStore"
    private const val FAVORITES_FILE = "favorites.json"
    private const val CUSTOM_FILE = "custom_quotes.json"

    val favorites = mutableListOf<String>()
    val customQuotes = mutableListOf<String>()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        favorites.clear()
        favorites.addAll(loadList(context, FAVORITES_FILE))
        customQuotes.clear()
        customQuotes.addAll(loadList(context, CUSTOM_FILE))
        initialized = true
        Log.d(TAG, "Загружено: favorites=${favorites.size}, custom=${customQuotes.size}")
    }

    fun saveFavorites(context: Context) {
        saveList(context, FAVORITES_FILE, favorites)
    }

    fun saveCustomQuotes(context: Context) {
        saveList(context, CUSTOM_FILE, customQuotes)
    }

    fun removeCustomQuote(context: Context, quote: String, author: String): Boolean {
        val key = "$quote|$author"
        val removed = customQuotes.remove(key)
        if (removed) {
            saveCustomQuotes(context)
            Log.i(TAG, "Удалена своя цитата: $quote")
        } else {
            Log.w(TAG, "Попытка удалить чужую цитату: $quote")
        }
        return removed
    }

    private fun loadList(context: Context, fileName: String): List<String> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            val array = JSONArray(text)
            val result = mutableListOf<String>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val quote = obj.optString("quote", "")
                val author = obj.optString("author", "")
                if (quote.isNotEmpty()) result.add("$quote|$author")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка чтения $fileName", e)
            emptyList()
        }
    }

    private fun saveList(context: Context, fileName: String, list: List<String>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val parts = item.split("|", limit = 2)
                val obj = JSONObject()
                obj.put("quote", parts.getOrElse(0) { "" })
                obj.put("author", parts.getOrElse(1) { "" })
                array.put(obj)
            }
            File(context.filesDir, fileName).writeText(array.toString())
            Log.d(TAG, "Сохранено в $fileName: ${list.size} записей")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка записи $fileName", e)
        }
    }
}