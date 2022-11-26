package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import ga.jundbits.ensan7ayawanshay2.R;

public class HelperMethods {

    private static String currentUserID = "";

    public static DocumentReference getAppDocument(Activity activity) {
        return FirebaseFirestore.getInstance().collection(activity.getString(R.string.app_name)).document("AppCollections");
    }

    public static CollectionReference getUsersCollection(Activity activity) {
        return getAppDocument(activity).collection("Users");
    }

    public static DocumentReference getUserDocument(Activity activity, String userID) {
        return getUsersCollection(activity).document(userID);
    }

    public static void setCurrentUserOnline(Activity activity, boolean online) {
        if (currentUserID.isEmpty())
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserDocument(activity, currentUserID).update("online", online);
    }

    public static long getCurrentTimestamp() {

        Timestamp timestamp = Timestamp.now();
        long seconds = timestamp.getSeconds();
        long nanoseconds = timestamp.getNanoseconds();
        long secondsToMillis = TimeUnit.SECONDS.toMillis(seconds);
        long nanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(nanoseconds);
        return secondsToMillis + nanoSecondsToMillis;

    }

}
