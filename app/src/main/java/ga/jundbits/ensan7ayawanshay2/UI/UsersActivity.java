package ga.jundbits.ensan7ayawanshay2.UI;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ga.jundbits.ensan7ayawanshay2.Adapters.UsersRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.FirebaseHelper;
import ga.jundbits.ensan7ayawanshay2.Utils.SingletonRequestQueue;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;

public class UsersActivity extends UserOnlineActivity implements UsersRecyclerAdapter.Callback {

    // UI
    private Toolbar usersToolbar;
    private RecyclerView usersRecyclerView;
    private AdView usersAdView;

    // long
    private long timestampTotalMillis;

    // String
    private String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private String serverKey = "key=" + "AAAAkMH0wIw:APA91bHeisXwbFx_KFlEc-n1mIaIeEAy-QQloTnVfPETjK1uk0x8lfcviiSEUM7VtjGzeL03EeDokxvOGhXjKTJSa723exN-AnsYxMZPmjKeqN1Pi-tu2zWTxVDBoIfLgMji9Ljq4IWo";
    private String contentType = "application/json";
    private String topicName, notificationTitle, notificationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        initVars();
        setupToolbar();
        loadUsers();

    }

    private void initVars() {

        // UI
        usersToolbar = findViewById(R.id.users_toolbar);
        usersRecyclerView = findViewById(R.id.users_recycler_view);
        usersAdView = findViewById(R.id.users_ad_view);

        // Google
        AdMob.requestBannerAd(usersAdView);

        // long
        timestampTotalMillis = getIntent().getLongExtra("timestamp_total_millis", 0);

    }

    private void setupToolbar() {

        setSupportActionBar(usersToolbar);
        getSupportActionBar().setTitle(getString(R.string.create_room));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadUsers() {

        Query usersQuery = FirebaseHelper.usersCollection.orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<UsersModel> options = new FirestoreRecyclerOptions.Builder<UsersModel>()
                .setLifecycleOwner(this)
                .setQuery(usersQuery, UsersModel.class)
                .build();

        UsersRecyclerAdapter adapter = new UsersRecyclerAdapter(options, this, this, timestampTotalMillis);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);

    }

    @Override
    public void sendInvitation(String userID, String name, long timestampTotalMillis) {

        topicName = "/topics/GamesInvitations_" + userID;
        notificationTitle = getString(R.string.new_game_invitation);
        notificationMessage = name + " " + getString(R.string.invited_you_to_a_game);

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try {

            notificationBody.put("title", notificationTitle);
            notificationBody.put("message", notificationMessage);
            notificationBody.put("content", timestampTotalMillis);

            notification.put("to", topicName);
            notification.put("data", notificationBody);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendNotification(notification);

        Map<String, Integer> scoresMap = new HashMap<>();
        scoresMap.put(FirebaseHelper.currentUserID, 0);

        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("players", FieldValue.arrayUnion(FirebaseHelper.currentUserID));
        roomMap.put("scores", scoresMap);
        roomMap.put("started", false);
        roomMap.put("first_start", true);
        roomMap.put("letter", "A");
        roomMap.put("timestamp_millis", timestampTotalMillis);

        FirebaseHelper.appDocument
                .collection("Rooms").document(String.valueOf(timestampTotalMillis))
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            FirebaseHelper.appDocument
                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
                                    .update(
                                            "players", FieldValue.arrayUnion(userID),
                                            "scores." + userID, 0
                                    );

                        } else {

                            FirebaseHelper.appDocument
                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
                                    .set(roomMap)
                                    .addOnSuccessListener(UsersActivity.this, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            FirebaseHelper.appDocument
                                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
                                                    .update(
                                                            "players", FieldValue.arrayUnion(userID),
                                                            "scores." + userID, 0
                                                    );

                                        }
                                    });

                        }

                    }
                });

    }

    private void sendNotification(JSONObject notification) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API_URL, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;

            }
        };

        SingletonRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

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