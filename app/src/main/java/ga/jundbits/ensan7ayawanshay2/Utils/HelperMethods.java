package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;

public class HelperMethods {

    private static RequestQueue requestQueue;
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

    public static <T> void addToRequestQueue(Context context, Request<T> request) {

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);

        requestQueue.add(request);

    }

}
