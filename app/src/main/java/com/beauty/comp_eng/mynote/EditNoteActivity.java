package com.beauty.comp_eng.mynote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditNoteActivity extends AppCompatActivity {

    private String action;
    private EditText noteEditor;
    private EditText titleEditor;
    private String noteFilter;
    private String oldText;
    private String oldTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteEditor = (EditText) findViewById(R.id.editText);
        noteEditor.requestFocus();
        titleEditor = (EditText) findViewById(R.id.editTitle);
        //titleEditor.setText("Hello World!");
        Intent intent = getIntent();
        // Allows you to pass complex object as an intent extra
        Uri uri = intent.getParcelableExtra(NoteProvider.CONTENT_ITEM_TYPE);

        // null when uri isn't passed in, else, request to edit note

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();     // where clause
            // cursor gives you access to one record that matched requested primary key value
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
//            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY));
            cursor.moveToFirst();   // retrieve data
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_BODY));  // gets text of selected note
            noteEditor.setText(oldText);    // sets the text in EditNoteActivity activity to the old text for editing
            oldTitle = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TITLE));
            titleEditor.setText(oldTitle);
            noteEditor.requestFocus();      // sends cursor to end of text
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            // When toolbar back button is pressed
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
        }

        return true;    // true means you always handled the menu selection
    }

    private void finishEditing() {
        String newText = noteEditor.getText().toString().trim();
        String newTitle = titleEditor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    /* Sends message to MainActivity saying that operation that was requested is
                    cancelled if the length of the new note is 0.
                     */
                    setResult(RESULT_CANCELED);
                } else {
                    if (newText.length() == 0) {
                        insertNote("", newTitle);
                    } else if (newTitle.length() == 0) {
                        insertNote(newText, "");
                    } else {
                        insertNote(newText, newTitle);
                    }
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0 && newTitle.length() == 0) {
                    deleteNote();
                } else if (oldText.equals(newText) && oldTitle.equals(newTitle)) {
                    setResult(RESULT_CANCELED);     // if there are no changes, send back the RESULT_CANCELLED value
                } else {
                    if (newText.length() == 0) {
                        updateNote("", newTitle);
                    } else if (newTitle.length() == 0) {
                        updateNote(newText, "");
                    } else {
                        updateNote(newText, newTitle);
                    }
                }
        }
        finish();   // finished with activity, then go to parent activity
    }

    private void deleteNote() {
        getContentResolver().delete(NoteProvider.CONTENT_URI, noteFilter, null);
        Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void updateNote(String noteText, String titleText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, titleText);
        // reusing noteFilter value to make sure you're only updating 1 selected row
        getContentResolver().update(NoteProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);   // send message to MainActivity saying something has changed, update content in list
    }

    // Code from MainActivity insertNote method
    private void insertNote(String noteText, String titleText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_BODY, noteText);
        values.put(DBOpenHelper.NOTE_TITLE, titleText);
        getContentResolver().insert(NoteProvider.CONTENT_URI, values);  // insert method returns Uri but we can ignore it
        setResult(RESULT_OK);   // means the operation that was requested is completed
    }

    // When Android back button is pressed (toolbar back button in onOptionsItemSelected method
    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
