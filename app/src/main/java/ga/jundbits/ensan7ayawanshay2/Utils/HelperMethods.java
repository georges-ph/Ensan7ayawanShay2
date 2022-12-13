package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import ga.jundbits.ensan7ayawanshay2.Callbacks.HelperMethodsCallback;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;

public class HelperMethods {

    private static UsersModel currentUserModel;

    public static UsersModel getCurrentUserModel() {
        return currentUserModel;
    }

    public static void setCurrentUserModel(UsersModel currentUserModel) {
        HelperMethods.currentUserModel = currentUserModel;
    }

    public static String getCurrentUserID() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser.getUid();
    }

    public static DocumentReference appDocumentRef(Context context) {
        return FirebaseFirestore.getInstance().collection(context.getString(R.string.app_name)).document("AppCollections");
    }

    public static CollectionReference usersCollectionRef(Context context) {
        return appDocumentRef(context).collection("Users");
    }

    public static DocumentReference userDocumentRef(Context context, String userID) {
        return usersCollectionRef(context).document(userID);
    }

    public static CollectionReference roomsCollectionRef(Context context) {
        return appDocumentRef(context).collection("Rooms");
    }

    public static DocumentReference roomDocumentRef(Context context, String roomID) {
        return roomsCollectionRef(context).document(roomID);
    }

    public static CollectionReference entriesCollectionRef(Context context, String roomID) {
        return roomDocumentRef(context, roomID).collection("Entries");
    }

    public static DocumentReference entriesDocumentRef(Context context, String roomID) {
        return entriesCollectionRef(context, roomID).document(getCurrentUserID());
    }

    private static DatabaseReference getUserRef(String userID) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        return firebaseDatabase.getReference().child("users_online").child(userID);
    }

    public static void setCurrentUserOnline(boolean online) {
        getUserRef(HelperMethods.getCurrentUserID()).setValue(online);
    }

    public static long getCurrentTimestamp() {

        Timestamp timestamp = Timestamp.now();
        long seconds = timestamp.getSeconds();
        long nanoseconds = timestamp.getNanoseconds();
        long secondsToMillis = TimeUnit.SECONDS.toMillis(seconds);
        long nanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(nanoseconds);
        return secondsToMillis + nanoSecondsToMillis;

    }

    public static String chooseLetter() {

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

    public static void getUserData(Context context, String userID, HelperMethodsCallback callback) {

        userDocumentRef(context, userID)
                .get()
                .addOnSuccessListener((Activity) context, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        UsersModel usersModel = documentSnapshot.toObject(UsersModel.class);
                        callback.onSuccess(usersModel);

                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        callback.onFailure(e);

                    }
                });

    }

    public static void isUserOnline(String userID, HelperMethodsCallback callback) {

        getUserRef(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Boolean online = snapshot.getValue(Boolean.class);
                callback.isOnline(online);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static void requestBannerAd(AdView adView) {

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    public static void requestInterstitialAd(Context context, HelperMethodsCallback callback) {

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.interstitial_game_room_ad_id), adRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                callback.onAdLoaded(interstitialAd);

            }

        });

    }

}
