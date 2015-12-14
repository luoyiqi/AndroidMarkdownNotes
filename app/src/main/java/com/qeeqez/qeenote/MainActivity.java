package com.qeeqez.qeenote;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qeeqez.qeenote.database.DBOpenHelper;
import com.qeeqez.qeenote.database.NotesCursorAdapter;
import com.qeeqez.qeenote.database.NotesProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;
    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cursorAdapter = new NotesCursorAdapter(this, null, 0);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);

        list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);
        list.setEmptyView(emptyText);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
        registerForContextMenu(list);
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            String[] menuItems = {getResources().getString(R.string.context_add), getResources()
                    .getString(R.string.context_view), getResources().getString(R.string
                    .context_edit), getResources().getString(R.string.context_remove)
            };
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        if (menuItemIndex == 0) {
            openEditorForNewNote(list);
        }

        if (menuItemIndex == 1) {
            Intent intent = new Intent(MainActivity.this, ViewActivity.class);
            Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + cursorAdapter
                    .getItemId(info.position));
            intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
            startActivityForResult(intent, EDITOR_REQUEST_CODE);
        }

        if (menuItemIndex == 2) {
            Intent intent = new Intent(MainActivity.this, EditorActivity.class);
            Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + cursorAdapter
                    .getItemId(info.position));
            intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
            startActivityForResult(intent, EDITOR_REQUEST_CODE);
        }

        if (menuItemIndex == 3) {
            getContentResolver().delete(NotesProvider.CONTENT_URI,
                    DBOpenHelper.NOTE_ID + "=" + cursorAdapter
                            .getItemId(info.position), null);
            Toast.makeText(this, getString(R.string.note_deleted),
                    Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            restartLoader();
        }
        return true;
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().insert(NotesProvider.CONTENT_URI,
                values);
    }

    private void removeAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            openEditorForNewNote(list);
            return true;
        }
        if (id == R.id.action_remove_all) {
            removeAllNotes();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openEditorForNewNote(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }


    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotesProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK) {
            restartLoader();
        }
    }

    @Override
    protected void onResume() {
        restartLoader();
        super.onResume();
    }

}
