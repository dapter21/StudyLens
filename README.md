# StudyLens

StudyLens is an Android application designed to help students convert physical or image-based notes into searchable digital study material.

## Features

- Scan notes using the device camera
- Select note images from the device
- Extract text using Google ML Kit OCR
- Create custom titles for notes
- Save notes locally
- View saved notes
- Search notes by title or content
- Delete notes
- Generate a basic summary of saved notes

## Technologies Used

- Kotlin
- Android Studio
- XML
- Google ML Kit Text Recognition
- Room Database
- RecyclerView
- Android Activity Result APIs

## How It Works

1. The user scans a note or selects an image.
2. StudyLens processes the image using Google ML Kit.
3. Extracted text is displayed to the user.
4. The user enters a title and saves the note.
5. Saved notes are stored locally using Room Database.
6. Users can search, open, summarize, or delete their saved notes.

## Project Structure

- `MainActivity` handles the home screen, image selection, camera access, OCR, and note search.
- `ResultActivity` displays extracted text and saves notes.
- `NoteDetailActivity` displays individual notes and provides summary and delete functionality.
- `Note` defines the Room database entity.
- `NoteDao` handles database operations.
- `NoteDatabase` manages the Room database.
- `NoteAdapter` displays saved notes in the RecyclerView.

## Future Improvements

- AI-powered summarization
- Automatic quiz generation
- Better handwritten-text recognition
- Cloud synchronization
- Improved UI and accessibility
