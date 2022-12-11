package ga.jundbits.ensan7ayawanshay2.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.preference.PreferenceManager;

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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.FirebaseHelper;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class MainActivity extends AppCompatActivity {

    // UI
    private ConstraintLayout mainLayout;
    private ProgressBar mainProgressBar;
    private ImageView mainImage;
    private Button mainStartButton, mainSettingsButton;

    // Google
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    // Firebase
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    // int
    private static final int REQ_CODE_ONE_TAP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadTheme();
        initVars();
        loadData();
        setOnClicks();

    }

    private void loadTheme() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString("theme", "");

        if (theme.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

    }

    private void initVars() {

        // UI
        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar = findViewById(R.id.main_progress_bar);
        mainImage = findViewById(R.id.main_image);
        mainStartButton = findViewById(R.id.main_start_button);
        mainSettingsButton = findViewById(R.id.main_settings_button);

        // Google
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setFilterByAuthorizedAccounts(false) // false to show all signed in accounts on the device
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .build())
                .build();

        // Firebase
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60 * 10) // 10 minutes
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_remote_config_defaults);

    }

    private void loadData() {

        fetchRemoteConfig();

        // TODO: migrating to helper methods separately
        new FirebaseHelper(this);

        MobileAds.initialize(this);

        createNotificationChannels();

        if (mainStartButton.getVisibility() == View.GONE) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    loadDataEnded();

                }
            }, 2000);

        }

        if (getIntent().hasExtra("invitation_id"))
            mainStartButton.setText(getString(R.string.join_room));

    }

    private void fetchRemoteConfig() {

        firebaseRemoteConfig.fetchAndActivate()
                .addOnSuccessListener(this, new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                        boolean enabled = firebaseRemoteConfig.getBoolean("e7s_start_button_enabled");
                        mainStartButton.setEnabled(enabled);

                    }
                });

    }

    private void createNotificationChannels() {

        // TODO: 27-Nov-22 Guess should check for Google Play services
        //  https://firebase.google.com/docs/cloud-messaging/android/client?hl=en&authuser=2#sample-pla

        // TODO: 27-Nov-22 Request notification permission for Android 13+
        //  https://firebase.google.com/docs/cloud-messaging/android/client?hl=en&authuser=2#request-permission13

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
                        mainStartButton.setVisibility(View.VISIBLE);
                        mainSettingsButton.setVisibility(View.VISIBLE);
                    }
                })
                .playOn(mainImage);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);
        constraintSet.setVerticalBias(R.id.main_start_button, (float) 0.7);
        constraintSet.applyTo(mainLayout);

    }

    private void setOnClicks() {

        mainStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainProgressBar.setVisibility(View.VISIBLE);
                mainStartButton.setEnabled(false);
                checkIfSignedIn();

            }
        });

        mainSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);

            }
        });

    }

    private void checkIfSignedIn() {

        if (firebaseUser != null) {
            goToStart();
        } else {

            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                        @Override
                        public void onSuccess(BeginSignInResult beginSignInResult) {

                            try {
                                startIntentSenderForResult(beginSignInResult.getPendingIntent().getIntentSender(), REQ_CODE_ONE_TAP, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {

                                mainProgressBar.setVisibility(View.GONE);
                                mainStartButton.setEnabled(true);

                                // TODO: this should be shown on the console after few hours
                                //  I think I should find an alternative to log events in real time
                                // from here
                                String stackTrace = "";

                                for (int i = 0; i < e.getStackTrace().length; i++) {
                                    stackTrace += e.getStackTrace()[i] + "\n";
                                }

                                Bundle bundle = new Bundle();
                                bundle.putString("exception_message", e.getMessage());
                                bundle.putString("stack_trace", stackTrace);

                                FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("app_error", bundle);
                                // to here

                                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mainProgressBar.setVisibility(View.GONE);
                            mainStartButton.setEnabled(true);

                            Toast.makeText(MainActivity.this, "No accounts found on this device", Toast.LENGTH_SHORT).show();

                        }
                    });

        }

    }

    private void goToStart() {

        mainProgressBar.setVisibility(View.GONE);
        mainStartButton.setEnabled(true);

        if (getIntent().hasExtra("invitation_id")) {

            long gameID = getIntent().getLongExtra("invitation_id", 0);

            Intent gameRoomIntent = new Intent(this, GameRoomActivity.class);
            gameRoomIntent.putExtra("game_id", gameID);
            startActivity(gameRoomIntent);
            finish();

            return;

        }

        Intent startIntent = new Intent(this, StartActivity.class);
        startActivity(startIntent);

    }

    private void firebaseAuthGoogleSignIn(SignInCredential credential) {

        AuthCredential authCredential = GoogleAuthProvider.getCredential(credential.getGoogleIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        firebaseUser = authResult.getUser();
                        registerWithFirebase(credential);

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mainProgressBar.setVisibility(View.GONE);
                        mainStartButton.setEnabled(true);

                        Toast.makeText(MainActivity.this, getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void registerWithFirebase(SignInCredential credential) {

        String userID = firebaseUser.getUid();

        UsersModel usersModel = new UsersModel(
                userID,
                credential.getDisplayName(),
                credential.getId(),
                String.valueOf(credential.getProfilePictureUri()),
                "",
                true);

        // Retrieving token here to ensure it is available and not null when proceeding
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        usersModel.setToken(token);

                    }
                });

        HelperMethods.userDocumentRef(this, userID)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {

                            mainStartButton.performClick();

                        } else {

                            HelperMethods.userDocumentRef(MainActivity.this, userID)
                                    .set(usersModel)
                                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mainStartButton.performClick();

                                        }
                                    })
                                    .addOnFailureListener(MainActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            mainProgressBar.setVisibility(View.GONE);
                                            mainStartButton.setEnabled(true);

                                            Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();

                                        }
                                    });

                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mainProgressBar.setVisibility(View.GONE);
                        mainStartButton.setEnabled(true);

                        Toast.makeText(MainActivity.this, getString(R.string.try_again), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_ONE_TAP) {

            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                if (credential.getGoogleIdToken() != null) {
                    firebaseAuthGoogleSignIn(credential);
                }
            } catch (ApiException e) {

                mainProgressBar.setVisibility(View.GONE);
                mainStartButton.setEnabled(true);

                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_SHORT).show();

            }

        }

    }

}