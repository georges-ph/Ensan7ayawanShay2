package ga.jundbits.ensan7ayawanshay2.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.FirebaseHelper;

public class MainActivity extends AppCompatActivity {

    // UI
    private ConstraintLayout mainLayout;
    private ProgressBar mainProgressBar;
    private ImageView mainImage;
    private Button mainButton;

    // Google
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // Firebase
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    // int
    private static final int REQ_ONE_TAP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initVars();
        loadData();
        setOnClicks();

    }

    private void initVars() {

        // UI
        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar = findViewById(R.id.main_progress_bar);
        mainImage = findViewById(R.id.main_image);
        mainButton = findViewById(R.id.main_button);

        // Google
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .build())
                .build();

        // Firebase
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(600) // 10 minutes
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_remote_config_defaults);

    }

    private void loadData() {

        fetchRemoteConfig();

        new FirebaseHelper(this);

        MobileAds.initialize(this);

        createNotificationChannels();

        if (mainButton.getVisibility() == View.GONE) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    loadDataEnded();

                }
            }, 2000);

        }

        if (getIntent().hasExtra("game_id"))
            mainButton.setText(getString(R.string.join_room));

    }

    private void fetchRemoteConfig() {

        firebaseRemoteConfig.fetchAndActivate()
                .addOnSuccessListener(this, new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                        boolean enabled = firebaseRemoteConfig.getBoolean("e7s_start_button_enabled");
                        mainButton.setEnabled(enabled);

                    }
                });

    }

    private void createNotificationChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.games_invitations_notification_channel_id),
                    "Games Invitations",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }

    }

    private void loadDataEnded() {

        YoYo
                .with(Techniques.FadeInUp)
                .duration(500)
                .withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mainButton.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(mainImage);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);
        constraintSet.setVerticalBias(R.id.main_button, (float) 0.7);
        constraintSet.applyTo(mainLayout);

    }

    private void setOnClicks() {

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainProgressBar.setVisibility(View.VISIBLE);
                mainButton.setEnabled(false);
                checkIfSignedIn();

            }
        });

    }

    private void checkIfSignedIn() {

        if (firebaseUser == null) {

            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                        @Override
                        public void onSuccess(BeginSignInResult beginSignInResult) {

                            try {
                                startIntentSenderForResult(beginSignInResult.getPendingIntent().getIntentSender(), REQ_ONE_TAP, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    });

        } else {

            goToStart();

        }

    }

    private void firebaseAuthGoogleSignIn(String idToken, String name, String email, Uri
            imageUri) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        firebaseUser = authResult.getUser();
                        registerWithFirebase(firebaseUser.getUid(), name, email, String.valueOf(imageUri));

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mainProgressBar.setVisibility(View.GONE);
                        mainButton.setEnabled(true);

                        Toast.makeText(MainActivity.this, getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void registerWithFirebase(String userID, String name, String email, String image) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userID);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("image", image);
        userMap.put("online", true);

        FirebaseHelper.usersCollection.document(userID)
                .set(userMap)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadData();
                        mainButton.performClick();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mainProgressBar.setVisibility(View.GONE);
                        mainButton.setEnabled(true);

                        Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void goToStart() {

        mainProgressBar.setVisibility(View.GONE);
        mainButton.setEnabled(true);

        if (getIntent().hasExtra("game_id")) {

            long gameID = getIntent().getLongExtra("game_id", 0);

            Intent gameRoomIntent = new Intent(this, GameRoomActivity.class);
            gameRoomIntent.putExtra("game_id", gameID);
            startActivity(gameRoomIntent);
            finish();

            return;

        }

        Intent startIntent = new Intent(this, StartActivity.class);
        startActivity(startIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {

            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    firebaseAuthGoogleSignIn(idToken, credential.getDisplayName(), credential.getId(), credential.getProfilePictureUri());
                }
            } catch (ApiException e) {

                mainProgressBar.setVisibility(View.GONE);
                mainButton.setEnabled(true);

                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_SHORT).show();

            }

        }

    }

}