package com.example.studylens

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_note_detail)

        val detailTitle =
            findViewById<TextView>(R.id.detailTitle)

        val detailContent =
            findViewById<TextView>(R.id.detailContent)

        val summaryTitle =
            findViewById<TextView>(R.id.summaryTitle)

        val summaryContent =
            findViewById<TextView>(R.id.summaryContent)

        val summaryButton =
            findViewById<Button>(R.id.summaryButton)

        val deleteButton =
            findViewById<Button>(R.id.deleteButton)

        val detailBackButton =
            findViewById<Button>(R.id.detailBackButton)

        val noteId =
            intent.getIntExtra("note_id", -1)

        val title =
            intent.getStringExtra("note_title")

        val content =
            intent.getStringExtra("note_content")

        detailTitle.text =
            title ?: "Note"

        detailContent.text =
            content ?: "No content available"

        summaryButton.setOnClickListener {

            if (content.isNullOrBlank()) {

                Toast.makeText(
                    this,
                    "No content available to summarize",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val summary =
                createSummary(content)

            summaryTitle.visibility =
                View.VISIBLE

            summaryContent.visibility =
                View.VISIBLE

            summaryContent.text =
                summary
        }

        deleteButton.setOnClickListener {

            if (noteId != -1) {

                val database =
                    NoteDatabase.getDatabase(this)

                val note =
                    Note(
                        id = noteId,
                        title = title ?: "Note",
                        content = content ?: ""
                    )

                CoroutineScope(Dispatchers.IO).launch {

                    database.noteDao().deleteNote(note)

                    runOnUiThread {

                        Toast.makeText(
                            this@NoteDetailActivity,
                            "Note deleted",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                }

            } else {

                Toast.makeText(
                    this,
                    "Unable to delete note",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        detailBackButton.setOnClickListener {
            finish()
        }
    }

    private fun createSummary(text: String): String {

        val cleanedText =
            text
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        if (cleanedText.isEmpty()) {
            return "No summary available."
        }

        val sentences =
            cleanedText
                .split(
                    Regex("(?<=[.!?])\\s+")
                )
                .map { sentence ->
                    sentence.trim()
                }
                .filter { sentence ->
                    sentence.length >= 25
                }

        if (sentences.isEmpty()) {
            return "The note is too short to summarize."
        }

        if (sentences.size <= 3) {

            return sentences.joinToString("\n\n") { sentence ->
                "• $sentence"
            }
        }

        val keywords =
            listOf(
                "important",
                "main",
                "define",
                "definition",
                "means",
                "used",
                "known",
                "consists",
                "includes",
                "process",
                "method",
                "advantage",
                "disadvantage",
                "example",
                "because",
                "therefore",
                "purpose",
                "function"
            )

        val scoredSentences =
            sentences.mapIndexed { index, sentence ->

                val lowerSentence =
                    sentence.lowercase()

                var score = 0

                keywords.forEach { keyword ->

                    if (lowerSentence.contains(keyword)) {
                        score += 2
                    }
                }

                if (sentence.length in 40..200) {
                    score += 1
                }

                if (index == 0) {
                    score += 1
                }

                Pair(
                    sentence,
                    score
                )
            }

        val selectedSentences =
            scoredSentences
                .sortedByDescending { pair ->
                    pair.second
                }
                .take(5)
                .sortedBy { pair ->
                    sentences.indexOf(pair.first)
                }
                .map { pair ->
                    pair.first
                }

        return selectedSentences.joinToString("\n\n") { sentence ->
            "• $sentence"
        }
    }
}