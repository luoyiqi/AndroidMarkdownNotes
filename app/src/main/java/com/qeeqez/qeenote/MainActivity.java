package com.qeeqez.qeenote;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.qeeqez.qeenote.database.DBOpenHelper;
import com.qeeqez.qeenote.database.NotesCursorAdapter;
import com.qeeqez.qeenote.database.NotesProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EDITOR_REQUEST_CODE = 1001;
    private CursorAdapter cursorAdapter;
    private SwipeMenuListView list;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cursorAdapter = new NotesCursorAdapter(this, null, 0);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);

        list = (SwipeMenuListView) findViewById(android.R.id.list);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem shareItem = new SwipeMenuItem(
                        getApplicationContext());
                shareItem.setBackground(new ColorDrawable(Color.parseColor("#4CAF50")));
                shareItem.setWidth(dp2px(90));
                shareItem.setIcon(R.drawable.ic_share_36dp);

                SwipeMenuItem editItem = new SwipeMenuItem(
                        getApplicationContext());
                editItem.setBackground(new ColorDrawable(Color.parseColor("#FFC107")));
                editItem.setWidth(dp2px(90));
                editItem.setIcon(R.drawable.ic_create_36dp);

                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.parseColor("#F44336")));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setIcon(R.drawable.ic_delete_36dp);

                menu.addMenuItem(shareItem);
                menu.addMenuItem(editItem);
                menu.addMenuItem(deleteItem);

            }
        };
        list.setMenuCreator(creator);

        list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + cursorAdapter
                        .getItemId(position));
                switch (index) {
                    case 0:
                       // Intent sendIntent = new Intent();
                       // sendIntent.setAction(Intent.ACTION_SEND);
                        Cursor cursor = getContentResolver().query(uri,null, DBOpenHelper.NOTE_ID + "=" + cursorAdapter
                                .getItemId(position), null, null);
                        cursor.moveToFirst();
                        System.out.println(cursor.getString(2));
                       // sendIntent.putExtra(Intent.EXTRA_TEXT, cursor.moveToFirst());
                      //  sendIntent.setType("text/plain");
                       // startActivity(sendIntent);
                        break;
                    case 1:
                        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                        intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                        startActivityForResult(intent, EDITOR_REQUEST_CODE);
                        break;
                    case 2:
                        final boolean[] isSnackClicked = {false};
                        list.getChildAt(position).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), (R.color.colorPrimary)));
                        list.getChildAt(position).findViewById(R.id.tvNote).setVisibility(View.GONE);
                        list.getChildAt(position).findViewById(R.id.tvNoteDate).setVisibility(View.GONE);
                        list.getChildAt(position).findViewById(R.id.tvNoteDeleted).setVisibility(View.VISIBLE);

                        setResult(RESULT_OK);
                        snackbar = Snackbar
                                .make(list, getResources().getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                                .setAction(getResources().getString(R.string.action_undo), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Snackbar snackbar1 = Snackbar.make(list, getResources().getString(R.string.note_restored), Snackbar.LENGTH_SHORT);
                                        snackbar1.show();
                                        isSnackClicked[0] = true;
                                        list.getChildAt(position).setBackgroundColor(Color.parseColor("#FAFAFA"));
                                        list.getChildAt(position).findViewById(R.id.tvNote).setVisibility(View.VISIBLE);
                                        list.getChildAt(position).findViewById(R.id.tvNoteDate).setVisibility(View.VISIBLE);
                                        list.getChildAt(position).findViewById(R.id.tvNoteDeleted).setVisibility(View.GONE);
                                        setResult(RESULT_OK);
                                    }
                                }).setCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        if (!isSnackClicked[0]) {
                                            getContentResolver().delete(NotesProvider.CONTENT_URI,
                                                    DBOpenHelper.NOTE_ID + "=" + cursorAdapter
                                                            .getItemId(position), null);
                                            restartLoader();
                                        }
                                    }
                                });
                        snackbar.show();
                        break;
                }
                return false;
            }
        });

        list.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);


        assert list != null;
        list.setAdapter(cursorAdapter);
        list.setEmptyView(emptyText);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.findViewById(R.id.tvNote).getVisibility() == View.VISIBLE) {
                    Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                    Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                    intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                    startActivityForResult(intent, EDITOR_REQUEST_CODE);
                }
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

    public int dp2px(float dips) {
        return (int) (dips * getResources().getDisplayMetrics().density + 0.5f);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_alphabet) {
            sortList("alphabet");
            return true;
        }
        if (id == R.id.action_sort_date) {
            sortList("date");
            return true;
        }
        if (id == R.id.action_backup) {
            Intent intent = new Intent(this, BackupActivity.class);
            startActivity(intent);
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

    void sortList(String action) {
        if (action.equals("alphabet")) {
            Cursor cursor = getContentResolver().query(NotesProvider.CONTENT_URI, null, null, null, DBOpenHelper.NOTE_TEXT + " ASC");
            cursorAdapter = new NotesCursorAdapter(this, cursor, 0);
            list.setAdapter(cursorAdapter);
            registerForContextMenu(list);
            restartLoader();
        }
        if (action.equals("date")) {
            getLoaderManager().restartLoader(2, null, this);
        }
    }

}
