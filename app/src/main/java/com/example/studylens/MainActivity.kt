package com.example.studylens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText

    private var allNotes: List<Note> = emptyList()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            if (uri != null) {
                recognizeTextFromUri(uri)
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {
                recognizeTextFromBitmap(bitmap)
            } else {
                Toast.makeText(
                    this,
                    "Photo was not captured",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        val scanButton =
            findViewById<Button>(R.id.scanButton)

        val chooseImageButton =
            findViewById<Button>(R.id.chooseImageButton)

        notesRecyclerView =
            findViewById(R.id.notesRecyclerView)

        searchEditText =
            findViewById(R.id.searchEditText)

        notesRecyclerView.layoutManager =
            LinearLayoutManager(this)

        scanButton.setOnClickListener {
            checkCameraPermission()
        }

        chooseImageButton.setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }

        searchEditText.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    filterNotes(text?.toString() ?: "")
                }

                override fun afterTextChanged(
                    text: android.text.Editable?
                ) {
                }
            }
        )

        loadNotes()
    }

    override fun onResume() {
        super.onResume()

        if (::notesRecyclerView.isInitialized) {
            loadNotes()
        }
    }

    private fun checkCameraPermission() {

        val permissionStatus =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {

            openCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun recognizeTextFromBitmap(
        bitmap: android.graphics.Bitmap
    ) {

        try {

            val image =
                InputImage.fromBitmap(
                    bitmap,
                    0
                )

            val recognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
                )

            recognizer.process(image)
                .addOnSuccessListener { result ->

                    val extractedText =
                        result.text

                    if (extractedText.isNotEmpty()) {

                        openResultScreen(extractedText)

                    } else {

                        Toast.makeText(
                            this,
                            "No text was detected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Text recognition failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        } catch (exception: Exception) {

            Toast.makeText(
                this,
                "Unable to process camera image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun recognizeTextFromUri(
        uri: android.net.Uri
    ) {

        try {

            val image =
                InputImage.fromFilePath(
                    this,
                    uri
                )

            val recognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS
                )

            recognizer.process(image)
                .addOnSuccessListener { result ->

                    val extractedText =
                        result.text

                    if (extractedText.isNotEmpty()) {

                        openResultScreen(extractedText)

                    } else {

                        Toast.makeText(
                            this,
                            "No text was detected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Text recognition failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        } catch (exception: Exception) {

            Toast.makeText(
                this,
                "Unable to read image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openResultScreen(text: String) {

        val intent =
            Intent(
                this,
                ResultActivity::class.java
            )

        intent.putExtra(
            "extracted_text",
            text
        )

        startActivity(intent)
    }

    private fun loadNotes() {

        val database =
            NoteDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {

            allNotes =
                database.noteDao().getAllNotes()

            runOnUiThread {

                filterNotes(
                    searchEditText.text.toString()
                )
            }
        }
    }

    private fun filterNotes(query: String) {

        val searchQuery =
            query.trim().lowercase()

        val filteredNotes =

            if (searchQuery.isEmpty()) {

                allNotes

            } else {

                allNotes.filter { note ->

                    note.title.lowercase()
                        .contains(searchQuery) ||

                            note.content.lowercase()
                                .contains(searchQuery)
                }
            }

        notesRecyclerView.adapter =
            NoteAdapter(filteredNotes)
    }
}