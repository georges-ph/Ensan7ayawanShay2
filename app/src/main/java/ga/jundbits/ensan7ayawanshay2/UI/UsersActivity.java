package ga.jundbits.ensan7ayawanshay2.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ga.jundbits.ensan7ayawanshay2.Adapters.UsersRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Callbacks.UsersRecyclerAdapterCallback;
import ga.jundbits.ensan7ayawanshay2.Models.GameModel;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.Utils.Secrets;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;
import io.sentry.Scope;
import io.sentry.ScopeCallback;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class UsersActivity extends UserOnlineActivity implements UsersRecyclerAdapterCallback {

    // UI
    private Toolbar usersToolbar;
    private RecyclerView usersRecyclerView;
    private FloatingActionButton usersFab;
    private AdView usersAdView;

    // long
    private long timestampMillis;

    // String
    // still using legacy http method and not http v1 api cause didn't know how to make it work
    private String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";

    // For invitations
    private final List<UsersModel> invitedUsers = new ArrayList<>();
    private final List<String> playersID = new ArrayList<>();
    private final Map<String, Integer> scoresMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        initVars();
        setupToolbar();
        loadUsers();
        setOnClicks();

    }

    private void initVars() {

        // UI
        usersToolbar = findViewById(R.id.users_toolbar);
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersFab = findViewById(R.id.users_fab);
        usersAdView = findViewById(R.id.users_ad_view);

        // Google
        HelperMethods.requestBannerAd(usersAdView);

        // long
        timestampMillis = getIntent().getLongExtra("timestamp_millis", 0);

    }

    private void setupToolbar() {

        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle(getString(R.string.create_room));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadUsers() {

        Query usersQuery = HelperMethods.usersCollectionRef(this).orderBy("name", Query.Direction.ASCENDING);
        usersQuery.get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (queryDocumentSnapshots.isEmpty())
                            return;

                        List<UsersModel> usersList = queryDocumentSnapshots.toObjects(UsersModel.class);

                        UsersRecyclerAdapter adapter = new UsersRecyclerAdapter(UsersActivity.this, usersList, UsersActivity.this);

                        usersRecyclerView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
                        usersRecyclerView.setAdapter(adapter);

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Sentry.captureException(e);

                        Toast.makeText(UsersActivity.this, "Error getting users", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public void invitationList(List<UsersModel> invitedUsers) {

        this.invitedUsers.clear();
        playersID.clear();
        scoresMap.clear();

        this.invitedUsers.add(HelperMethods.getCurrentUserModel());
        this.invitedUsers.addAll(invitedUsers);

        for (UsersModel usersModel : this.invitedUsers) {
            playersID.add(usersModel.getId());
            scoresMap.put(usersModel.getId(), 0);
        }

        Collections.sort(playersID, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

    }

    private void setOnClicks() {

        usersFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (UsersModel usersModel : invitedUsers) {

                    if (usersModel.getId().equals(HelperMethods.getCurrentUserID()) || !usersModel.isNotifications())
                        continue;

                    sendInvitationNotification(usersModel.getFcm_token());

                }

                createRoomInFirebase();

            }
        });

    }

    private void sendInvitationNotification(String token) {

        String notificationTitle = getString(R.string.new_game_invitation);
        String notificationBody = HelperMethods.getCurrentUserModel().getName() + " " + getString(R.string.invited_you_to_a_game);

        JSONObject notification = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            /*
            JSON format:
                {
                    "notification": {
                        "body": "body",
                        "title": "title"
                    },
                    "data":{
                        "some_param": "some_value",
                        // example
                        "text": "Hello World!"
                    },
                    "to": "target_user_fcm_token"
                }
             */

            notification.put("title", notificationTitle);
            notification.put("body", notificationBody);
            data.put("invitation_id", timestampMillis);
            json.put("data", data);
            json.put("notification", notification);
            json.put("to", token);

        } catch (JSONException e) {

            Sentry.captureException(e, new ScopeCallback() {
                @Override
                public void run(@NotNull Scope scope) {

                    User user = new User();
                    user.setId(HelperMethods.getCurrentUserID());

                    scope.setUser(user);

                }
            });

            return;

        }

        OkHttpClient client = new OkHttpClient();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(FCM_API_URL)
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString()))
                .addHeader("Authorization", "key=" + Secrets.SERVER_KEY)
                // .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Sentry.captureException(e, new ScopeCallback() {
                    @Override
                    public void run(@NotNull Scope scope) {

                        User user = new User();
                        user.setId(HelperMethods.getCurrentUserID());

                        scope.setUser(user);

                    }
                });

            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                }

            }
        });

    }

    private void createRoomInFirebase() {

        GameModel gameModel = new GameModel(true, false, "A", HelperMethods.getCurrentUserID(), playersID, scoresMap, timestampMillis);

        HelperMethods
                .roomDocumentRef(getApplicationContext(), String.valueOf(timestampMillis))
                .set(gameModel)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Toast.makeText(UsersActivity.this, "Invitations sent successfully", Toast.LENGTH_SHORT).show();

                        Intent gameRoomIntent = new Intent(UsersActivity.this, GameRoomActivity.class);
                        gameRoomIntent.putExtra("game_id", timestampMillis);
                        startActivity(gameRoomIntent);
                        finish();

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return false;

        }

    }

}