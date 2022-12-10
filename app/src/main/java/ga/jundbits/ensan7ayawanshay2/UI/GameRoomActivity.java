package ga.jundbits.ensan7ayawanshay2.UI;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Adapters.GameRoomEntriesRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Adapters.GameRoomPlayersRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Callbacks.HelperMethodsCallback;
import ga.jundbits.ensan7ayawanshay2.Enums.FieldType;
import ga.jundbits.ensan7ayawanshay2.Models.EntriesModel;
import ga.jundbits.ensan7ayawanshay2.Models.GameModel;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;

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

    // int
    private final int ZERO = 0, FIVE = 5, TEN = 10;
    private int ensanScore = 0, hayawanScore = 0, shay2Score = 0, totalScore = 0;

    // Firebase References
    private DocumentReference gameRoomDocument;

    // Models
    private GameModel gameModel;

    // Lists
    private List<String> namesList = new ArrayList<>();

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
        gameRoomDocument = HelperMethods.roomDocumentRef(this, String.valueOf(gameID));

    }

    private void setupToolbar() {

        setSupportActionBar(gameRoomToolbar);
        getSupportActionBar().setTitle(getString(R.string.game_room));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadGame() {

        gameRoomDocument
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                        if (documentSnapshot == null || !documentSnapshot.exists())
                            return;

                        gameModel = documentSnapshot.toObject(GameModel.class);

                        loadLetter();
                        loadPlayers();

                    }
                });

    }

    private void loadLetter() {

        if (gameModel.isStarted()) {

            gameRoomLetter.setText(gameModel.getLetter());

            gameRoomEnsan.getEditText().getText().clear();
            gameRoom7ayawan.getEditText().getText().clear();
            gameRoomShay2.getEditText().getText().clear();

            gameRoomEnsan.setEnabled(true);
            gameRoom7ayawan.setEnabled(true);
            gameRoomShay2.setEnabled(true);

            // this is making the document updates go crazy
//            Map<String, Integer> scores = gameModel.getScores();
//            int score = scores.get(HelperMethods.getCurrentUserID());
//            scores.put(HelperMethods.getCurrentUserID(), score + totalScore);
//            gameModel.setScores(scores);
//
//            gameRoomDocument.set(gameModel);

            // this works fine
            // maybe because it's updating a single field instead of the whole map and whole document
            gameRoomDocument
                    .update("scores." + HelperMethods.getCurrentUserID(), FieldValue.increment(totalScore));

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

            if (gameModel.isFirst_start()) {

                gameRoomLetter.setText(getString(R.string.press_start_button));

            } else {

                gameRoomLetter.setText(gameModel.getLetter() + getString(R.string.stopped));

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

    private void clearEntries() {

        HelperMethods.entriesDocumentRef(this, String.valueOf(gameID))
                .set(new EntriesModel().empty());

    }

    private void saveEntries() {

        String ensan = gameRoomEnsan.getEditText().getText().toString();
        String hayawan = gameRoom7ayawan.getEditText().getText().toString();
        String shay2 = gameRoomShay2.getEditText().getText().toString();

        if (!TextUtils.isEmpty(ensan) || !TextUtils.isEmpty(hayawan) || !TextUtils.isEmpty(shay2)) {

            EntriesModel entriesModel = new EntriesModel(ensan, hayawan, shay2);

            HelperMethods.entriesDocumentRef(this, String.valueOf(gameID))
                    .set(entriesModel);

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPlayersEntries();
            }
        }, 1000); // delay the loading until namesList is not empty

    }

    private void loadPlayersEntries() {

        List<String> ensanList = new ArrayList<>();
        List<String> hayawanList = new ArrayList<>();
        List<String> shay2List = new ArrayList<>();

        HelperMethods.entriesCollectionRef(this, String.valueOf(gameID))
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        int index = 0;

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                            EntriesModel entriesModel = documentSnapshot.toObject(EntriesModel.class);

                            ensanList.add(namesList.get(index) + ": " + entriesModel.getEnsan());
                            hayawanList.add(namesList.get(index) + ": " + entriesModel.getHayawan());
                            shay2List.add(namesList.get(index) + ": " + entriesModel.getShay2());

                            index++;

                            if (ensanList.size() == queryDocumentSnapshots.size())
                                createList(ensanList, gameRoomEnsanEntries);

                            if (hayawanList.size() == queryDocumentSnapshots.size())
                                createList(hayawanList, gameRoom7ayawanEntries);

                            if (shay2List.size() == queryDocumentSnapshots.size())
                                createList(shay2List, gameRoomShay2Entries);

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
        List<Integer> scoresList = new ArrayList<>();

        namesList.clear();

        for (int i = 0; i < gameModel.getPlayers().size(); i++) {

            String playerID = gameModel.getPlayers().get(i);

            HelperMethods.getUserData(this, playerID, new HelperMethodsCallback() {
                @Override
                public void onSuccess(UsersModel usersModel) {

                    playersIdList.add(playerID);
                    namesList.add(usersModel.getName());
                    scoresList.add(gameModel.getScores().get(playerID));

                    if (namesList.size() == gameModel.getPlayers().size()) {

                        GameRoomPlayersRecyclerAdapter gameRoomPlayersRecyclerAdapter = new GameRoomPlayersRecyclerAdapter(GameRoomActivity.this, playersIdList, namesList, scoresList);

                        gameRoomPlayersRecyclerView.setLayoutManager(new LinearLayoutManager(GameRoomActivity.this));
                        gameRoomPlayersRecyclerView.setAdapter(gameRoomPlayersRecyclerAdapter);

                    }

                }

                @Override
                public void onFailure(Exception e) {

                }

                @Override
                public void isOnline(Boolean online) {

                }
            });

        }

    }

    private void setOnClick() {

        gameRoomStartButton.setOnClickListener(v -> startGame());
        gameRoomStopButton.setOnClickListener(v -> stopGame());

        gameRoomEnsanPoints0.setOnClickListener(v -> clickedScore(FieldType.ENSAN, ZERO));
        gameRoomEnsanPoints5.setOnClickListener(v -> clickedScore(FieldType.ENSAN, FIVE));
        gameRoomEnsanPoints10.setOnClickListener(v -> clickedScore(FieldType.ENSAN, TEN));

        gameRoom7ayawanPoints0.setOnClickListener(v -> clickedScore(FieldType.HAYAWAN, ZERO));
        gameRoom7ayawanPoints5.setOnClickListener(v -> clickedScore(FieldType.HAYAWAN, FIVE));
        gameRoom7ayawanPoints10.setOnClickListener(v -> clickedScore(FieldType.HAYAWAN, TEN));

        gameRoomShay2Points0.setOnClickListener(v -> clickedScore(FieldType.SHAY2, ZERO));
        gameRoomShay2Points5.setOnClickListener(v -> clickedScore(FieldType.SHAY2, FIVE));
        gameRoomShay2Points10.setOnClickListener(v -> clickedScore(FieldType.SHAY2, TEN));

    }

    private void clickedScore(FieldType type, int score) {

        if (type.equals(FieldType.ENSAN)) {

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

        } else if (type.equals(FieldType.HAYAWAN)) {

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

        } else if (type.equals(FieldType.SHAY2)) {

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

        gameModel.setStarted(true);
        gameModel.setLetter(HelperMethods.chooseLetter());
        gameModel.setFirst_start(false);

        gameRoomDocument
                .set(gameModel);

    }

    private void stopGame() {

        gameModel.setStarted(false);

        gameRoomDocument
                .set(gameModel);

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