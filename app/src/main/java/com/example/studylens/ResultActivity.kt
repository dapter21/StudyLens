package com.example.studylens

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_result)

        val extractedText =
            findViewById<TextView>(R.id.extractedText)

        val noteTitleInput =
            findViewById<EditText>(R.id.noteTitleInput)

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val backButton =
            findViewById<Button>(R.id.backButton)

        val text =
            intent.getStringExtra("extracted_text")

        if (text != null) {
            extractedText.text = text
        } else {
            extractedText.text = "No text available"
        }

        saveButton.setOnClickListener {

            val title =
                noteTitleInput.text.toString().trim()

            if (text == null || text.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nothing to save",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (title.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter a note title",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val database =
                NoteDatabase.getDatabase(this)

            val note =
                Note(
                    title = title,
                    content = text
                )

            CoroutineScope(Dispatchers.IO).launch {

                database.noteDao().insertNote(note)

                runOnUiThread {

                    Toast.makeText(
                        this@ResultActivity,
                        "Note saved",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}