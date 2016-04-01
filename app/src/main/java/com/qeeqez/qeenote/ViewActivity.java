package com.qeeqez.qeenote;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qeeqez.qeenote.database.DBOpenHelper;
import com.qeeqez.qeenote.database.NotesProvider;

public class ViewActivity extends AppCompatActivity {

    private static final int EDITOR_REQUEST_CODE = 1001;
    private static String noteFilter;
    private String action;
    private String oldText;
    private TextView viewTV;
    private String idString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewTV = (TextView) findViewById(R.id.viewNoteTV);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        action = Intent.ACTION_VIEW;
        idString = uri.getLastPathSegment();
        noteFilter = DBOpenHelper.NOTE_ID + "=" + idString;
        Cursor cursor = getContentResolver().query(uri,
                DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
        cursor.moveToFirst();
        oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
        viewTV.setText(oldText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT,
                oldText);
        myShareActionProvider.setShareIntent(myShareIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                setResult(RESULT_OK);
                break;
            case R.id.action_delete:
                deleteNote();
                break;
        }

        return true;
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter, null);
        Toast.makeText(this, getString(R.string.note_deleted),
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    public void openEditor(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + idString);
        intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        action = Intent.ACTION_VIEW;
        idString = uri.getLastPathSegment();
        noteFilter = DBOpenHelper.NOTE_ID + "=" + idString;
        Cursor cursor = getContentResolver().query(uri,
                DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
        viewTV.setText(oldText);
        super.onResume();
    }

}
