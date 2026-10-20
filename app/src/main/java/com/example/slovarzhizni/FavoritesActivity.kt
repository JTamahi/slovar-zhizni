package com.example.slovarzhizni

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FavoritesActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "FavoritesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        Log.d(TAG, "onCreate")

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            Log.i(TAG, "Назад в главное меню")
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        render()
    }

    private fun render() {
        val container = findViewById<LinearLayout>(R.id.llFavorites)
        val emptyView = findViewById<TextView>(R.id.tvEmpty)
        container.removeAllViews()

        if (QuoteStore.favorites.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }
        emptyView.visibility = View.GONE

        val inflater = LayoutInflater.from(this)
        for (item in QuoteStore.favorites.toList()) {
            val parts = item.split("|", limit = 2)
            val quote = parts.getOrElse(0) { "" }
            val author = parts.getOrElse(1) { getString(R.string.unknown_author) }

            val row = inflater.inflate(R.layout.item_favorite, container, false)
            row.findViewById<TextView>(R.id.tvFavQuote).text = "«$quote»"
            row.findViewById<TextView>(R.id.tvFavAuthor).text = "— $author"
            row.findViewById<Button>(R.id.btnRemoveFavorite).setOnClickListener {
                QuoteStore.favorites.remove(item)
                QuoteStore.saveFavorites(this)
                Log.i(TAG, "Удалено: $quote")
                render()
            }
            container.addView(row)
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}