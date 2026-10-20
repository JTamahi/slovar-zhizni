package com.example.slovarzhizni

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
        const val EXTRA_NEW_QUOTE = "EXTRA_NEW_QUOTE"
        const val EXTRA_NEW_AUTHOR = "EXTRA_NEW_AUTHOR"
    }

    private var currentCategory = "all"
    private var currentQuote = ""
    private var currentAuthor = ""

    private lateinit var tvQuote: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvQuoteOfDay: TextView
    private lateinit var tvAuthorOfDay: TextView
    private lateinit var btnDeleteCustom: Button

    private val addQuoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val quote = result.data?.getStringExtra(EXTRA_NEW_QUOTE) ?: return@registerForActivityResult
        currentQuote = quote
        currentAuthor = result.data?.getStringExtra(EXTRA_NEW_AUTHOR)
            ?: getString(R.string.unknown_author)
        updateQuoteViews()
        updateDeleteButton()
        Log.i(TAG, "Вернулись с новой цитатой: $quote")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")

        QuoteStore.init(this)

        tvQuote = findViewById(R.id.tvQuote)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvQuoteOfDay = findViewById(R.id.tvQuoteOfDay)
        tvAuthorOfDay = findViewById(R.id.tvAuthorOfDay)
        btnDeleteCustom = findViewById(R.id.btnDeleteCustom)

        findViewById<Button>(R.id.btnAll).setOnClickListener { selectCategory("all") }
        findViewById<Button>(R.id.btnMotivation).setOnClickListener { selectCategory("motivation") }
        findViewById<Button>(R.id.btnWisdom).setOnClickListener { selectCategory("wisdom") }
        findViewById<Button>(R.id.btnHumor).setOnClickListener { selectCategory("humor") }
        findViewById<Button>(R.id.btnStudy).setOnClickListener { selectCategory("study") }
        findViewById<Button>(R.id.btnMyQuotes).setOnClickListener { selectCategory("custom") }

        findViewById<Button>(R.id.btnAnother).setOnClickListener { showRandomQuote() }
        findViewById<Button>(R.id.btnFavorite).setOnClickListener { addCurrentToFavorites() }
        findViewById<Button>(R.id.btnShare).setOnClickListener { shareCurrentQuote() }
        btnDeleteCustom.setOnClickListener { deleteCurrentCustomQuote() }

        findViewById<Button>(R.id.btnFavorites).setOnClickListener {
            Log.i(TAG, "Открываем избранное")
            startActivity(Intent(this, FavoritesActivity::class.java)
                .putExtra(EXTRA_CATEGORY, currentCategory))
        }
        findViewById<Button>(R.id.btnAddQuote).setOnClickListener {
            Log.i(TAG, "Открываем форму добавления")
            addQuoteLauncher.launch(Intent(this, AddQuoteActivity::class.java))
        }

        loadQuoteOfDay()
        showRandomQuote()
    }

    private fun loadQuoteOfDay() {
        val all = mutableListOf<Pair<String, String>>()
        all += pairs(R.array.quotes_motivation, R.array.authors_motivation)
        all += pairs(R.array.quotes_wisdom, R.array.authors_wisdom)
        all += pairs(R.array.quotes_humor, R.array.authors_humor)
        all += pairs(R.array.quotes_study, R.array.authors_study)
        if (all.isEmpty()) return

        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val (text, author) = all[day % all.size]

        tvQuoteOfDay.text = "«$text»"
        tvAuthorOfDay.text = "— $author"
        Log.d(TAG, "Мысль дня: $text")
    }

    private fun pairs(quotesRes: Int, authorsRes: Int): List<Pair<String, String>> {
        val q = resources.getStringArray(quotesRes)
        val a = resources.getStringArray(authorsRes)
        return q.indices.map { i ->
            q[i] to (if (i < a.size) a[i] else getString(R.string.unknown_author))
        }
    }

    private fun selectCategory(category: String) {
        currentCategory = category
        Log.i(TAG, "Категория: $category")
        showRandomQuote()
    }

    private fun quotesFor(category: String): Pair<Array<String>, Array<String>> {
        if (category == "custom") {
            val q = mutableListOf<String>()
            val a = mutableListOf<String>()
            for (item in QuoteStore.customQuotes) {
                val p = item.split("|", limit = 2)
                q.add(p.getOrElse(0) { "" })
                a.add(p.getOrElse(1) { getString(R.string.unknown_author) })
            }
            return q.toTypedArray() to a.toTypedArray()
        }

        if (category == "all") {
            val q = mutableListOf<String>()
            val a = mutableListOf<String>()
            q += resources.getStringArray(R.array.quotes_motivation)
            a += resources.getStringArray(R.array.authors_motivation)
            q += resources.getStringArray(R.array.quotes_wisdom)
            a += resources.getStringArray(R.array.authors_wisdom)
            q += resources.getStringArray(R.array.quotes_humor)
            a += resources.getStringArray(R.array.authors_humor)
            q += resources.getStringArray(R.array.quotes_study)
            a += resources.getStringArray(R.array.authors_study)
            return q.toTypedArray() to a.toTypedArray()
        }

        return when (category) {
            "motivation" -> resources.getStringArray(R.array.quotes_motivation) to
                    resources.getStringArray(R.array.authors_motivation)
            "wisdom" -> resources.getStringArray(R.array.quotes_wisdom) to
                    resources.getStringArray(R.array.authors_wisdom)
            "humor" -> resources.getStringArray(R.array.quotes_humor) to
                    resources.getStringArray(R.array.authors_humor)
            "study" -> resources.getStringArray(R.array.quotes_study) to
                    resources.getStringArray(R.array.authors_study)
            else -> emptyArray<String>() to emptyArray()
        }
    }

    private fun showRandomQuote() {
        val (quotes, authors) = quotesFor(currentCategory)
        if (quotes.isEmpty()) {
            currentQuote = ""
            currentAuthor = ""
            tvQuote.text = getString(R.string.error_no_custom_quotes)
            tvAuthor.text = ""
            updateDeleteButton()
            if (currentCategory == "custom") {
                Toast.makeText(this, R.string.error_no_custom_quotes, Toast.LENGTH_SHORT).show()
            }
            return
        }
        val i = (quotes.indices).random()
        currentQuote = quotes[i]
        currentAuthor = authors.getOrElse(i) { getString(R.string.unknown_author) }
        updateQuoteViews()
        updateDeleteButton()
        Log.d(TAG, "Случайная: $currentQuote")
    }

    private fun updateQuoteViews() {
        tvQuote.text = "«$currentQuote»"
        tvAuthor.text = "— $currentAuthor"
    }

    private fun updateDeleteButton() {
        val canDelete = currentCategory == "custom" &&
                currentQuote.isNotEmpty() &&
                QuoteStore.customQuotes.contains("$currentQuote|$currentAuthor")
        btnDeleteCustom.visibility = if (canDelete) View.VISIBLE else View.GONE
    }

    private fun deleteCurrentCustomQuote() {
        if (currentQuote.isEmpty()) return
        if (QuoteStore.removeCustomQuote(this, currentQuote, currentAuthor)) {
            Toast.makeText(this, R.string.quote_deleted, Toast.LENGTH_SHORT).show()
            showRandomQuote()
        }
    }

    private fun addCurrentToFavorites() {
        if (currentQuote.isEmpty()) return
        val key = "$currentQuote|$currentAuthor"
        if (QuoteStore.favorites.contains(key)) {
            Toast.makeText(this, R.string.error_already_favorite, Toast.LENGTH_SHORT).show()
            return
        }
        QuoteStore.favorites.add(key)
        QuoteStore.saveFavorites(this)
        Log.i(TAG, "В избранное: $currentQuote")
        Toast.makeText(this, R.string.favorite_added, Toast.LENGTH_SHORT).show()
    }

    private fun shareCurrentQuote() {
        if (currentQuote.isEmpty()) return
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("quote", "«$currentQuote» — $currentAuthor"))
        Log.i(TAG, "Скопировано: $currentQuote")
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}