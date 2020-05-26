package com.example.padtm_lab6;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameList extends AppCompatActivity {
    private int game;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        game = extras.getInt("gra");

        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshGameList();
                    }
                }
        );

        refreshGameList();


        ListView list = (ListView) findViewById(R.id.listView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
                spinner.setVisibility(View.VISIBLE);

                String game_id = arg0.getItemAtPosition(arg2).toString().replace("ID: ", "");


                Intent intencja = new Intent(
                        getApplicationContext(),
                        HttpServices.class);

                PendingIntent pendingResult = createPendingResult(HttpServices.GAME_INFO, new Intent(), 0);

                if (game == R.id.inRow) {
                    intencja.putExtra(HttpServices.URL, HttpServices.LINES + game_id);
                } else {
                    intencja.putExtra(HttpServices.URL, HttpServices.XO + game_id);
                }

                intencja.putExtra(HttpServices.METHOD, HttpServices.GET);

                intencja.putExtra(HttpServices.RETURN, pendingResult);

                startService(intencja);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intencja = null;
                switch (game) {
                    case R.id.inRow:
                        intencja = new Intent(getApplicationContext(), inRow.class);

                        intencja.putExtra(inRow.STATUS, inRow.NEW_GAME);

                        intencja.putExtra(inRow.MOVES, "");
                        break;
                    default:
                        // to do ticTac
                }
                startActivity(intencja);
            }
        });
  }

    public void refreshGameList(){
        ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        Snackbar.make(findViewById(R.id.main_list), getString(R.string.refresh), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        ListView list = (ListView) findViewById(R.id.listView);
        TextView emptyText = (TextView) findViewById(android.R.id.empty);



        Intent intencja = new Intent(
                getApplicationContext(),
                HttpServices.class);
        PendingIntent pendingResult = createPendingResult(HttpServices.GAMES_LIST, new Intent(), 0);
        if (game == R.id.inRow) {
            intencja.putExtra(HttpServices.URL, HttpServices.LINES);
        } else {
           //to do ticTac
        }
        intencja.putExtra(HttpServices.METHOD, HttpServices.GET);
        intencja.putExtra(HttpServices.RETURN, pendingResult);
        startService(intencja);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HttpServices.GAMES_LIST) {
            ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);
            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            swipeLayout.setRefreshing(false);

            try {
                JSONObject response = new JSONObject(data.getStringExtra(HttpServices.RESPONSE));

                if (response.getInt("games_count") > 0) {
                    TextView no_game = (TextView) findViewById(R.id.empty);
                    no_game.setVisibility(View.GONE);

                    JSONArray games = new JSONArray(response.getString("games"));
                    ArrayList<String> items = new ArrayList<String>();

                    for (int i = 0; i < response.getInt("games_count"); i++) {
                        JSONObject game = games.getJSONObject(i);
                        items.add("ID: " + game.getString("id"));
                    }

                    ArrayAdapter<String> gamesAdapter =
                            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
                    ListView list = (ListView) findViewById(R.id.listView);
                    list.setAdapter(gamesAdapter);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == HttpServices.GAME_INFO) {

            ProgressBar spinner = (ProgressBar) findViewById(R.id.progressBar1);
            spinner.setVisibility(View.GONE);

            if (game == R.id.inRow) {
                Intent intencja = new Intent(getApplicationContext(), inRow.class);

                try {
                    JSONObject response = new JSONObject(data.getStringExtra(HttpServices.RESPONSE));

                    intencja.putExtra(inRow.GAME_ID, response.getInt("id"));

                    if (response.getInt("status") == 0 && response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else if (response.getInt("status") == 1 && response.getInt("player1") == 1) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else if (response.getInt("status") == 2 && response.getInt("player1") == 2) {
                        intencja.putExtra(inRow.STATUS, inRow.YOUR_TURN);
                    } else
                        intencja.putExtra(inRow.STATUS, inRow.WAIT);


                    intencja.putExtra(inRow.PLAYER, response.getInt("player1"));
                    intencja.putExtra(inRow.MOVES, response.getString("moves"));
                    startActivity(intencja);

                } catch (Exception ex) {

                    ex.printStackTrace();
                }
            } else if (game == R.id.ticTacToe) {
                //to do ticTacToe
            }
        }
    }
}
