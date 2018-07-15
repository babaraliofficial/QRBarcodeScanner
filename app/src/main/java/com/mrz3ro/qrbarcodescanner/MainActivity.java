package com.mrz3ro.qrbarcodescanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    IntentIntegrator scan;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    List<ListItem> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean beep = sharedPref.getBoolean("beep",true);
        Boolean frontCamera = sharedPref.getBoolean("frontCamera",false);

        int camId;
        if(frontCamera == false)
            camId = 0;
        else
            camId = 1;
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listItems = new ArrayList<>();
        adapter = new MyAdapter(listItems, this);
        recyclerView.setAdapter(adapter);
        CardView cardView = findViewById(R.id.cardView);

        Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
        try {
            // Iterate Bunnys
            QueryResultIterable<Code> itr = cupboard().withCursor(codes).iterate(Code.class);
            for (Code bunny : itr) {
                // do something with bunny
                ListItem listItem = new ListItem( bunny._id,bunny.name,bunny.type);
                listItems.add(listItem);
                adapter = new MyAdapter(listItems, this);
                recyclerView.setAdapter(adapter);
            }
        } finally {
            // close the cursor
            codes.close();
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                ListItem item=listItems.get(position);

                cupboard().withDatabase(db).delete(Code.class,item.get_Id());
                listItems.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position,listItems.size());

            }
        }).attachToRecyclerView(recyclerView);
        scan = new IntentIntegrator(this);
        scan.setBeepEnabled(beep);
        scan.setCameraId(camId);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan.initiateScan();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (id == R.id.action_clearAll) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You are about to delete all records. Do you really want to proceed ?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    MyDatabaseHelper dbHelper = new MyDatabaseHelper(MainActivity.this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
                    try {
                        if (codes.getCount() > 0) {
                            cupboard().withDatabase(db).delete(Code.class, null);
                            listItems.clear();
                            adapter.notifyDataSetChanged();
                        } else {
                            return;
                        }
                    }finally {
                        codes.close();
                    }

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "You've changed your mind to delete all records", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();

        }
        if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        }

        if (id == R.id.action_generateQr){
            Intent genQrIntent = new Intent(this, GenerateQRActivity.class);
            startActivity(genQrIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            if (result.getContents() == null) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinate), "Result Not Found", Snackbar.LENGTH_LONG);

                snackbar.show();
            } else {

                Code codeObj = new Code(result.getContents(),result.getFormatName());
                MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = cupboard().withDatabase(db).put(codeObj);
                listItems.clear();
                adapter.notifyDataSetChanged();
                Cursor codes = cupboard().withDatabase(db).query(Code.class).orderBy( "_id DESC").getCursor();
                try {
                    // Iterate Bunnys
                    QueryResultIterable<Code> itr = cupboard().withCursor(codes).iterate(Code.class);
                    for (Code bunny : itr) {
                        // do something with bunny
                        ListItem listItem = new ListItem( bunny._id,bunny.name,bunny.type);
                        listItems.add(listItem);
                        adapter = new MyAdapter(listItems, this);
                        recyclerView.setAdapter(adapter);
                    }
                } finally {
                    // close the cursor
                    codes.close();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void cardClick(View card){

        TextView textView = findViewById(R.id.textViewCode);
        String code= textView.getText().toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, code);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("beep")) {
            scan.setBeepEnabled(sharedPreferences.getBoolean(key,true));
        }
        if (key.equals("frontCamera")) {
            int camId;
            if(sharedPreferences.getBoolean(key,false)== false)
                camId = 0;
            else
                camId = 1;
            scan.setCameraId(camId);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
