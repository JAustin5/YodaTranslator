package edu.cofc.android.yodatranslator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import edu.cofc.android.yodatranslator.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject
import kotlin.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.translationButton.setOnClickListener {
            val englishText = binding.lineInput.text.toString()
            ioScope.launch {
                val yodaStr = getYodaString(englishText)
                withContext(Dispatchers.Main) {
                    binding.translatedLine.text = yodaStr
                }
            }
        }

        binding.lineInput.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private suspend fun getYodaString(englishText: String): String {
        var yodaStr: String
        val baseUrl = "https://api.funtranslations.com/translate/yoda.json?text="
        val encodedEnglishText = withContext(Dispatchers.IO) {
            URLEncoder.encode(englishText, "UTF-8")
        }
        val url = URL(baseUrl + encodedEnglishText)

        try {
            val yodaJSON = url.readText()
            val jsonObj = JSONObject(yodaJSON)

            if (jsonObj.has("success")) {
                val contents = jsonObj.getJSONObject("contents")
                yodaStr = contents.getString("translated")
            }
            else {
                yodaStr = resources.getString(R.string.translation_failed)
            }
        }
        catch (ex : Exception) {
            Log.e("Yoda Translation", ex.stackTraceToString())
            yodaStr = "${ex.message}"
        }
        return yodaStr
    }
}