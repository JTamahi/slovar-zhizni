package com.example.slovarzhizni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddQuoteActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddQuoteActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_quote)
        Log.d(TAG, "onCreate")

        val tilQuote = findViewById<TextInputLayout>(R.id.tilQuote)
        val etQuote = findViewById<TextInputEditText>(R.id.etQuote)
        val etAuthor = findViewById<TextInputEditText>(R.id.etAuthor)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val text = etQuote.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) {
                tilQuote.error = getString(R.string.error_empty_quote)
                Log.w(TAG, "Пустой ввод")
                return@setOnClickListener
            }
            tilQuote.error = null

            val author = etAuthor.text?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: getString(R.string.unknown_author)

            QuoteStore.customQuotes.add("$text|$author")
            QuoteStore.saveCustomQuotes(this)
            Log.i(TAG, "Сохранено: $text")

            val data = Intent()
                .putExtra(MainActivity.EXTRA_NEW_QUOTE, text)
                .putExtra(MainActivity.EXTRA_NEW_AUTHOR, author)
            setResult(RESULT_OK, data)
            finish()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            Log.i(TAG, "Отмена ввода")
            finish()
        }
    }

    override fun onStart() { super.onStart(); Log.d(TAG, "onStart") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy") }
}