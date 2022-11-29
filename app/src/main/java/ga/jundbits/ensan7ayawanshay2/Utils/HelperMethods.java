package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

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

    public interface HelperMethodsCallback {
        void onSuccess(UsersModel usersModel);

        void onFailure(Exception e);
    }

}
