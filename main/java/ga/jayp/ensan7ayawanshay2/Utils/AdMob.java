package ga.jayp.ensan7ayawanshay2.Utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import ga.jayp.ensan7ayawanshay2.R;

public class AdMob {

    public static void requestBannerAd(AdView adView) {

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    public static void requestInterstitialAd(Context context, Callback callback) {

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, context.getString(R.string.interstitial_game_room_ad_id), adRequest, new InterstitialAdLoadCallback() {

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                callback.onAdLoaded(interstitialAd);

            }

        });

    }

    public interface Callback {
        void onAdLoaded(InterstitialAd interstitialAd);
    }

}
