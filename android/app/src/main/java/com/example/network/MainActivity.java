package com.example.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "NETWORK";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            // Note: this method is invoked on the main thread.
                            if (call.method.equals("getNetworkInfo")) {
                                String networkInfo = networkInfo();

                                if (networkInfo != null) {
                                    result.success(networkInfo);
                                } else {
                                    result.error("UNAVAILABLE", "Network info not available.", null);
                                }
                            } else {
                                result.notImplemented();
                            }
                        }

                );
    }

    private String networkInfo() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return "Wi-Fi network";
                case ConnectivityManager.TYPE_MOBILE:
                    return "Mobile network";
                default:
                    return "Not connected";
            }
        } else {
            return "Unknown";
        }
    }
}
