package ga.jundbits.ensan7ayawanshay2.UI;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Adapters.UsersRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class UsersActivity extends UserOnlineActivity implements UsersRecyclerAdapter.Callback {

    // UI
    private Toolbar usersToolbar;
    private RecyclerView usersRecyclerView;
    private AdView usersAdView;

    // long
    private long timestampTotalMillis;

    // String
    // still using legacy http method and not http v1 api cause didn't know how to make it work
    private String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private String serverKey = "AAAASjmaMiw:APA91bEum4z2usH6gddBuf4bxeRdDR18-HNFtASmIUfGlVrgd_2dAqDlWAaFgvYNsb414lcyGJaiwvMTLix6lVagXtsuOcBlxES7ZU-yq5x4J9Ms2JLoqvdxHG_aOy1o72-sXwbIKmI_";

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
        timestampTotalMillis = getIntent().getLongExtra("timestamp_millis", 0);

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
                        Toast.makeText(UsersActivity.this, "Error getting users", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void inviteUser(UsersModel usersModel) {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_game_invitation))
                .setMessage(getString(R.string.invite) + " " + usersModel.getName() + " " + getString(R.string.to_a_new_room))
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> sendInvitation(usersModel))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void sendInvitation(UsersModel usersModel) {

        String userID = usersModel.getId();
        String name = usersModel.getName();

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
            data.put("invitation_id", timestampTotalMillis);
            json.put("data", data);
            json.put("notification", notification);
            json.put("to", usersModel.getToken());

        } catch (JSONException e) {
            String st = "";
            for (int i = 0; i < e.getStackTrace().length; i++) {
                st += e.getStackTrace()[i];
            }
            Log.d("msggg", "exception: \nmessage: " + e.getMessage() + "\ncause: " + e.getCause() + "\nstacktrace: " + st);
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        okhttp3.Request request1 = new okhttp3.Request.Builder()
                .url(FCM_API_URL)
                .post(body)
                .addHeader("Authorization", "key=" + serverKey)
                // .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                String st = "";
                for (int i = 0; i < e.getStackTrace().length; i++) {
                    st += e.getStackTrace()[i];
                }
                Log.d("msggg", "error: \nmessage: " + e.getMessage() + "\ncause: " + e.getCause() + "\nstacktrace: " + st);


            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    Log.d("msggg", "response: " + responseBody.string());
                }


            }
        });

//        Map<String, Integer> scoresMap = new HashMap<>();
//        scoresMap.put(FirebaseHelper.currentUserID, 0);
//
//        Map<String, Object> roomMap = new HashMap<>();
//        roomMap.put("players", FieldValue.arrayUnion(FirebaseHelper.currentUserID));
//        roomMap.put("scores", scoresMap);
//        roomMap.put("started", false);
//        roomMap.put("first_start", true);
//        roomMap.put("letter", "A");
//        roomMap.put("timestamp_millis", timestampTotalMillis);
//
//        FirebaseHelper.appDocument
//                .collection("Rooms").document(String.valueOf(timestampTotalMillis))
//                .get()
//                .addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                        if (documentSnapshot.exists()) {
//
//                            FirebaseHelper.appDocument
//                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
//                                    .update(
//                                            "players", FieldValue.arrayUnion(userID),
//                                            "scores." + userID, 0
//                                    );
//
//                        } else {
//
//                            FirebaseHelper.appDocument
//                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
//                                    .set(roomMap)
//                                    .addOnSuccessListener(UsersActivity.this, new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//
//                                            FirebaseHelper.appDocument
//                                                    .collection("Rooms").document(String.valueOf(timestampTotalMillis))
//                                                    .update(
//                                                            "players", FieldValue.arrayUnion(userID),
//                                                            "scores." + userID, 0
//                                                    );
//
//                                        }
//                                    });
//
//                        }
//
//                    }
//                });

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