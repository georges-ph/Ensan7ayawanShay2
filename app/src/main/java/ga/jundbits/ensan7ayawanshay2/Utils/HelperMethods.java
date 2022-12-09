package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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

    public static void setCurrentUserOnline(Context context, boolean online) {
        userDocumentRef(context, getCurrentUserID()).update("online", online);
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

    public static void isUserOnline(Context context, String userID, HelperMethodsCallback callback) {

        userDocumentRef(context, userID)
                .addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            callback.onFailure(error);
                            return;
                        }

                        if (documentSnapshot == null || !documentSnapshot.exists())
                            return;

                        UsersModel usersModel = documentSnapshot.toObject(UsersModel.class);
                        callback.isOnline(usersModel.isOnline());

                    }
                });

    }

}
