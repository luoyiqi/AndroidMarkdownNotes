package com.qeeqez.qeenote;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;

import com.qeeqez.qeenote.database.DBOpenHelper;
import com.qeeqez.qeenote.database.NotesCursorAdapter;
import com.qeeqez.qeenote.database.NotesProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupActivity extends AppCompatActivity {
    Button btnBackup;
    Button btnRestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnBackup = (Button) findViewById(R.id.btn_backup);
        btnRestore = (Button) findViewById(R.id.btn_restore);
        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


//    public String getAllDataAndGenerateJSON() throws JSONException, FileNotFoundException {
//
//      Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_URI);
//
//
//        Cursor c = getContentResolver().query(uri,
//                DBOpenHelper.ALL_COLUMNS, null, null, null);
//        CursorAdapter cursorAdapter = new   NotesCursorAdapter(this, null, 0);
//        c.moveToFirst();
//        JSONObject root = new JSONObject();
//        JSONArray notesArray = new JSONArray();
//        DateFormat format = new SimpleDateFormat("yyyy.MM.dd");
//        File f = new File(Environment.getExternalStorageDirectory()
//                + "/Notes" + format.format(new Date()) + ".txt");
//        FileOutputStream fos = new FileOutputStream(f, true);
//        PrintStream ps = new PrintStream(fos);
//
//
//        int i = 0;
//        while (!c.isAfterLast()) {
//            JSONObject contact = new JSONObject();
//            try {
//                contact.put("id", c.getString(c.getColumnIndex(DBOpenHelper.NOTE_ID)));
//                contact.put("text", c.getString(c.getColumnIndex(DBOpenHelper.NOTE_TEXT)));
//                contact.put("date", c.getString(c.getColumnIndex(DBOpenHelper.NOTE_CREATED)));
//                c.moveToNext();
//
//                notesArray.put(i, contact);
//                i++;
//
//            } catch (JSONException e) {
//
//                e.printStackTrace();
//            }
//
//
//        }
//        root.put("NOTES", notesArray);
//        ps.append(root.toString());
//        return root.toString();
//    }
}
