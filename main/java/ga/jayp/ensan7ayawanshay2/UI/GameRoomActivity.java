package ga.jayp.ensan7ayawanshay2.UI;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ga.jayp.ensan7ayawanshay2.Adapters.GameRoomEntriesRecyclerAdapter;
import ga.jayp.ensan7ayawanshay2.Adapters.GameRoomPlayersRecyclerAdapter;
import ga.jayp.ensan7ayawanshay2.R;
import ga.jayp.ensan7ayawanshay2.Utils.AdMob;
import ga.jayp.ensan7ayawanshay2.Utils.FirebaseHelper;
import ga.jayp.ensan7ayawanshay2.Utils.UserOnlineActivity;

public class GameRoomActivity extends UserOnlineActivity {

    // UI
    private Toolbar gameRoomToolbar;
    private TextView gameRoomLetter, gameRoomYourScore;
    private RecyclerView gameRoomPlayersRecyclerView;
    private TextInputLayout gameRoomEnsan, gameRoom7ayawan, gameRoomShay2;
    private LinearLayout gameRoomEnsanPointsLayout, gameRoom7ayawanPointsLayout, gameRoomShay2PointsLayout;
    private TextView gameRoomEnsanPoints0, gameRoom7ayawanPoints0, gameRoomShay2Points0, gameRoomEnsanPoints5, gameRoom7ayawanPoints5, gameRoomShay2Points5, gameRoomEnsanPoints10, gameRoom7ayawanPoints10, gameRoomShay2Points10;
    private RecyclerView gameRoomEnsanEntries, gameRoom7ayawanEntries, gameRoomShay2Entries;
    private Button gameRoomStartButton, gameRoomStopButton;

    // Google
    private InterstitialAd gameRoomInterstitialAd;

    // boolean
    private boolean backButtonPressed = false, toolbarBackButtonPressed = false;

    // long
    private long gameID;

    // enum
    private enum Type {
        ENSAN,
        HAYAWAN,
        SHAY2
    }

    // int
    private int ZERO = 0, FIVE = 5, TEN = 10;
    private int ensanScore = 0, hayawanScore = 0, shay2Score = 0, totalScore = 0;

    // Firebase References
    private DocumentReference gameRoomDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);

        initVars();
        setupToolbar();
        loadGame();
        setOnClick();

    }

    private void initVars() {

        // UI
        gameRoomToolbar = findViewById(R.id.game_room_toolbar);
        gameRoomLetter = findViewById(R.id.game_room_letter);
        gameRoomYourScore = findViewById(R.id.game_room_your_score);
        gameRoomPlayersRecyclerView = findViewById(R.id.game_room_players_recycler_view);
        gameRoomEnsan = findViewById(R.id.game_room_ensan);
        gameRoom7ayawan = findViewById(R.id.game_room_7ayawan);
        gameRoomShay2 = findViewById(R.id.game_room_shay2);
        gameRoomEnsanPointsLayout = findViewById(R.id.game_room_ensan_points_layout);
        gameRoom7ayawanPointsLayout = findViewById(R.id.game_room_7ayawan_points_layout);
        gameRoomShay2PointsLayout = findViewById(R.id.game_room_shay2_points_layout);
        gameRoomEnsanPoints0 = findViewById(R.id.game_room_ensan_points_0);
        gameRoom7ayawanPoints0 = findViewById(R.id.game_room_7ayawan_points_0);
        gameRoomShay2Points0 = findViewById(R.id.game_room_shay2_points_0);
        gameRoomEnsanPoints5 = findViewById(R.id.game_room_ensan_points_5);
        gameRoom7ayawanPoints5 = findViewById(R.id.game_room_7ayawan_points_5);
        gameRoomShay2Points5 = findViewById(R.id.game_room_shay2_points_5);
        gameRoomEnsanPoints10 = findViewById(R.id.game_room_ensan_points_10);
        gameRoom7ayawanPoints10 = findViewById(R.id.game_room_7ayawan_points_10);
        gameRoomShay2Points10 = findViewById(R.id.game_room_shay2_points_10);
        gameRoomEnsanEntries = findViewById(R.id.game_room_ensan_entries);
        gameRoom7ayawanEntries = findViewById(R.id.game_room_7ayawan_entries);
        gameRoomShay2Entries = findViewById(R.id.game_room_shay2_entries);
        gameRoomStartButton = findViewById(R.id.game_room_start_button);
        gameRoomStopButton = findViewById(R.id.game_room_stop_button);

        // Google
        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {

            @Override
            public void onAdDismissedFullScreenContent() {

                gameRoomInterstitialAd = null;

                closeActivity();

            }

        };

        AdMob.requestInterstitialAd(this, new AdMob.Callback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {

                gameRoomInterstitialAd = interstitialAd;

                gameRoomInterstitialAd.setFullScreenContentCallback(fullScreenContentCallback);

            }
        });

        // long
        gameID = getIntent().getLongExtra("game_id", 0);

        // Firebase References
        gameRoomDocument = FirebaseHelper.appDocument.collection("Rooms").document(String.valueOf(gameID));

    }

    private void setupToolbar() {

        setSupportActionBar(gameRoomToolbar);
        getSupportActionBar().setTitle(getString(R.string.game_room));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadGame() {

        loadLetter();
        loadPlayers();

    }

    private void loadLetter() {

        gameRoomDocument
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                        boolean started = documentSnapshot.getBoolean("started");
                        boolean firstStart = documentSnapshot.getBoolean("first_start");
                        String letter = documentSnapshot.getString("letter");

                        if (started) {

                            gameRoomLetter.setText(letter);

                            gameRoomEnsan.getEditText().getText().clear();
                            gameRoom7ayawan.getEditText().getText().clear();
                            gameRoomShay2.getEditText().getText().clear();

                            gameRoomEnsan.setEnabled(true);
                            gameRoom7ayawan.setEnabled(true);
                            gameRoomShay2.setEnabled(true);

                            gameRoomDocument
                                    .update("scores." + FirebaseHelper.currentUserID, FieldValue.increment(totalScore));

                            clearEntries();

                            ensanScore = ZERO;
                            hayawanScore = ZERO;
                            shay2Score = ZERO;
                            totalScore = ZERO;
                            gameRoomYourScore.setText(String.valueOf(totalScore));

                            gameRoomEnsanEntries.setVisibility(View.GONE);
                            gameRoom7ayawanEntries.setVisibility(View.GONE);
                            gameRoomShay2Entries.setVisibility(View.GONE);

                            gameRoomEnsanPointsLayout.setVisibility(View.GONE);
                            gameRoom7ayawanPointsLayout.setVisibility(View.GONE);
                            gameRoomShay2PointsLayout.setVisibility(View.GONE);

                            gameRoomStartButton.setVisibility(View.GONE);
                            gameRoomStopButton.setVisibility(View.VISIBLE);

                        } else {

                            if (firstStart) {

                                gameRoomLetter.setText(getString(R.string.press_start_button));

                            } else {

                                gameRoomLetter.setText(letter + getString(R.string.stopped));

                                gameRoomEnsan.setEnabled(false);
                                gameRoom7ayawan.setEnabled(false);
                                gameRoomShay2.setEnabled(false);

                                saveEntries();

                                gameRoomEnsanPointsLayout.setVisibility(View.VISIBLE);
                                gameRoom7ayawanPointsLayout.setVisibility(View.VISIBLE);
                                gameRoomShay2PointsLayout.setVisibility(View.VISIBLE);

                                gameRoomEnsanPoints0.setBackgroundTintList(null);
                                gameRoomEnsanPoints5.setBackgroundTintList(null);
                                gameRoomEnsanPoints10.setBackgroundTintList(null);
                                gameRoom7ayawanPoints0.setBackgroundTintList(null);
                                gameRoom7ayawanPoints5.setBackgroundTintList(null);
                                gameRoom7ayawanPoints10.setBackgroundTintList(null);
                                gameRoomShay2Points0.setBackgroundTintList(null);
                                gameRoomShay2Points5.setBackgroundTintList(null);
                                gameRoomShay2Points10.setBackgroundTintList(null);

                                gameRoomEnsanEntries.setVisibility(View.VISIBLE);
                                gameRoom7ayawanEntries.setVisibility(View.VISIBLE);
                                gameRoomShay2Entries.setVisibility(View.VISIBLE);

                                gameRoomStartButton.setVisibility(View.VISIBLE);
                                gameRoomStopButton.setVisibility(View.GONE);

                            }

                        }

                    }
                });

    }

    private void clearEntries() {

        Map<String, Object> entriesMap = new HashMap<>();
        entriesMap.put("ensan", "");
        entriesMap.put("7ayawan", "");
        entriesMap.put("shay2", "");

        gameRoomDocument
                .collection("Entries").document(FirebaseHelper.currentUserID)
                .set(entriesMap);

    }

    private void saveEntries() {

        String ensan = gameRoomEnsan.getEditText().getText().toString();
        String hayawan = gameRoom7ayawan.getEditText().getText().toString();
        String shay2 = gameRoomShay2.getEditText().getText().toString();

        if (TextUtils.isEmpty(ensan) && TextUtils.isEmpty(hayawan) && TextUtils.isEmpty(shay2)) {

            loadPlayersEntries();
            return;

        }

        Map<String, Object> entriesMap = new HashMap<>();
        entriesMap.put("ensan", ensan);
        entriesMap.put("7ayawan", hayawan);
        entriesMap.put("shay2", shay2);

        gameRoomDocument
                .collection("Entries").document(FirebaseHelper.currentUserID)
                .set(entriesMap)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        loadPlayersEntries();

                    }
                });

    }

    private void loadPlayersEntries() {

        List<String> ensanList = new ArrayList<>();
        List<String> hayawanList = new ArrayList<>();
        List<String> shay2List = new ArrayList<>();

        // Ensan
        Query ensanQuery = gameRoomDocument.collection("Entries").orderBy("ensan");
        ensanQuery
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            FirebaseHelper.getUserName(documentSnapshot.getId(), new FirebaseHelper.Callback() {
                                @Override
                                public void onFinished(String data) {

                                    ensanList.add(data + ": " + documentSnapshot.getString("ensan"));

                                    if (ensanList.size() == queryDocumentSnapshots.size())
                                        createList(ensanList, gameRoomEnsanEntries);

                                }

                                @Override
                                public void isOnline(boolean online) {

                                }
                            });

                        }

                    }

                });

        // 7ayawan
        Query hayawanQuery = gameRoomDocument.collection("Entries").orderBy("7ayawan");
        hayawanQuery
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            FirebaseHelper.getUserName(documentSnapshot.getId(), new FirebaseHelper.Callback() {
                                @Override
                                public void onFinished(String data) {

                                    hayawanList.add(data + ": " + documentSnapshot.getString("7ayawan"));

                                    if (hayawanList.size() == queryDocumentSnapshots.size())
                                        createList(hayawanList, gameRoom7ayawanEntries);

                                }

                                @Override
                                public void isOnline(boolean online) {

                                }
                            });

                        }

                    }
                });

        // Shay2
        Query shay2Query = gameRoomDocument.collection("Entries").orderBy("shay2");
        shay2Query
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            FirebaseHelper.getUserName(documentSnapshot.getId(), new FirebaseHelper.Callback() {
                                @Override
                                public void onFinished(String data) {

                                    shay2List.add(data + ": " + documentSnapshot.getString("shay2"));

                                    if (shay2List.size() == queryDocumentSnapshots.size())
                                        createList(shay2List, gameRoomShay2Entries);

                                }

                                @Override
                                public void isOnline(boolean online) {

                                }
                            });

                        }

                    }
                });

    }

    private void createList(List<String> list, RecyclerView recyclerView) {

        GameRoomEntriesRecyclerAdapter gameRoomEntriesRecyclerAdapter = new GameRoomEntriesRecyclerAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(gameRoomEntriesRecyclerAdapter);

    }

    private void loadPlayers() {

        List<String> playersIdList = new ArrayList<>();
        List<String> namesList = new ArrayList<>();
        List<Integer> scoresList = new ArrayList<>();

        gameRoomDocument
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                        List<String> playersList = (List<String>) documentSnapshot.get("players");

                        playersIdList.clear();
                        namesList.clear();
                        scoresList.clear();

                        for (int i = 0; i < playersList.size(); i++) {

                            int finalI = i;

                            FirebaseHelper.getUserName(playersList.get(i), new FirebaseHelper.Callback() {
                                @Override
                                public void onFinished(String data) {

                                    playersIdList.add(playersList.get(finalI));
                                    namesList.add(data);
                                    scoresList.add(documentSnapshot.getLong("scores." + playersList.get(finalI)).intValue());

                                    if (namesList.size() == playersList.size()) {

                                        GameRoomPlayersRecyclerAdapter gameRoomPlayersRecyclerAdapter = new GameRoomPlayersRecyclerAdapter(GameRoomActivity.this, playersIdList,namesList, scoresList);

                                        gameRoomPlayersRecyclerView.setLayoutManager(new LinearLayoutManager(GameRoomActivity.this));
                                        gameRoomPlayersRecyclerView.setAdapter(gameRoomPlayersRecyclerAdapter);

                                    }

                                }

                                @Override
                                public void isOnline(boolean online) {

                                }
                            });
                        }

                    }
                });

    }

    private void setOnClick() {

        gameRoomStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startGame();

            }
        });

        gameRoomStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopGame();

            }
        });

        gameRoomEnsanPoints0.setOnClickListener(v -> clickedScore(Type.ENSAN, ZERO));
        gameRoomEnsanPoints5.setOnClickListener(v -> clickedScore(Type.ENSAN, FIVE));
        gameRoomEnsanPoints10.setOnClickListener(v -> clickedScore(Type.ENSAN, TEN));

        gameRoom7ayawanPoints0.setOnClickListener(v -> clickedScore(Type.HAYAWAN, ZERO));
        gameRoom7ayawanPoints5.setOnClickListener(v -> clickedScore(Type.HAYAWAN, FIVE));
        gameRoom7ayawanPoints10.setOnClickListener(v -> clickedScore(Type.HAYAWAN, TEN));

        gameRoomShay2Points0.setOnClickListener(v -> clickedScore(Type.SHAY2, ZERO));
        gameRoomShay2Points5.setOnClickListener(v -> clickedScore(Type.SHAY2, FIVE));
        gameRoomShay2Points10.setOnClickListener(v -> clickedScore(Type.SHAY2, TEN));

    }

    private void clickedScore(Type type, int score) {

        if (type.equals(Type.ENSAN)) {

            gameRoomEnsanPoints0.setBackgroundTintList(null);
            gameRoomEnsanPoints5.setBackgroundTintList(null);
            gameRoomEnsanPoints10.setBackgroundTintList(null);

            if (score == ZERO) {

                ensanScore = ZERO;
                gameRoomEnsanPoints0.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == FIVE) {

                ensanScore = FIVE;
                gameRoomEnsanPoints5.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == TEN) {

                ensanScore = TEN;
                gameRoomEnsanPoints10.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            }

        } else if (type.equals(Type.HAYAWAN)) {

            gameRoom7ayawanPoints0.setBackgroundTintList(null);
            gameRoom7ayawanPoints5.setBackgroundTintList(null);
            gameRoom7ayawanPoints10.setBackgroundTintList(null);

            if (score == ZERO) {

                hayawanScore = ZERO;
                gameRoom7ayawanPoints0.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == FIVE) {

                hayawanScore = FIVE;
                gameRoom7ayawanPoints5.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == TEN) {

                hayawanScore = TEN;
                gameRoom7ayawanPoints10.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            }

        } else if (type.equals(Type.SHAY2)) {

            gameRoomShay2Points0.setBackgroundTintList(null);
            gameRoomShay2Points5.setBackgroundTintList(null);
            gameRoomShay2Points10.setBackgroundTintList(null);

            if (score == ZERO) {

                shay2Score = ZERO;
                gameRoomShay2Points0.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == FIVE) {

                shay2Score = FIVE;
                gameRoomShay2Points5.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            } else if (score == TEN) {

                shay2Score = TEN;
                gameRoomShay2Points10.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));

            }

        }

        totalScore = ensanScore + hayawanScore + shay2Score;
        gameRoomYourScore.setText(String.valueOf(totalScore));

    }

    private void startGame() {

        Map<String, Object> gameMap = new HashMap<>();
        gameMap.put("started", true);
        gameMap.put("letter", chooseLetter());
//        gameMap.put("scores." + FirebaseHelper.currentUserID, FieldValue.increment(totalScore));

        gameRoomDocument
                .update(gameMap);

    }

    private String chooseLetter() {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] chars = alphabet.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 1; i++) {
            char c1 = chars[random.nextInt(chars.length)];
            stringBuilder.append(c1);
        }

        return stringBuilder.toString();

    }

    private void stopGame() {

        gameRoomDocument
                .update(
                        "started", false,
                        "first_start", false
                );

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                backButtonPressed = false;
                toolbarBackButtonPressed = true;

                showAd();

                return true;

            default:
                return false;

        }

    }

    @Override
    public void onBackPressed() {

        backButtonPressed = true;
        toolbarBackButtonPressed = false;

        showAd();

    }

    private void showAd() {

        if (gameRoomInterstitialAd != null)
            gameRoomInterstitialAd.show(this);
        else
            closeActivity();

    }

    private void closeActivity() {

        if (backButtonPressed)
            GameRoomActivity.super.onBackPressed();

        if (toolbarBackButtonPressed)
            finish();

    }

}