package com.example.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.NetworkRequest.Builder;
import android.os.Build;
import android.os.Build.VERSION;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;

public final class NetworkStreamHandler implements StreamHandler {
    private final NetworkCallback networkCallback;
    // 3. объявляем обратный вызов события
    private EventSink eventSink;
    private Activity activity;

    // 4. Внутри onListen() назначаем eventSink и вызываем startListeningNetworkChanges():
    public void onListen(Object arguments, EventSink events) {
        eventSink = events;
        startListeningNetworkChanges();
    }

    // 8. Чтобы закрыть канал событий, нам нужно перестать слушать изменения в сети и обнулить поля
    public void onCancel(Object arguments) {
        stopListeningNetworkChanges();
        eventSink = null;
        activity = null;
    }

    private void startListeningNetworkChanges() {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (manager != null) {
                manager.registerDefaultNetworkCallback(networkCallback);
            }
        } else {
            NetworkRequest request = new Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            if (manager != null) {
                manager.registerNetworkCallback(request, networkCallback);
            }
        }
    }

    private void stopListeningNetworkChanges() {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            manager.unregisterNetworkCallback(networkCallback);
        }
    }

    // 7. Фактическая отправка событий во Flutter происходит в networkCallback.
    public NetworkStreamHandler(Activity activity) {
        this.activity = activity;
        this.networkCallback = new NetworkCallback() {
            // отправляем сообщение Constants.disconnected обратному вызову eventSink всякий раз, когда соединение теряется.
            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // Сообщение Flutter, что сеть отключена
                // runOnUiThread используется, чтобы отправлялять события канала платформы в основной поток
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(eventSink != null) {
                            eventSink.success(Constants.DISCONNECTED);
                        }
                    }
                });
            }

            // затем проверяем состояние сети в методе onCapabilitiesChanged.
            // Если в сети есть WIFI, отправляем константу Wi-Fi.
            // Если у есть сотовая связь, отправляем константу сотовой связи.
            // В противном случае отправляем неизвестную константу.
            @Override
            public void onCapabilitiesChanged(Network network,NetworkCapabilities netCap) {
                super.onCapabilitiesChanged(network, netCap);
                // Pick the supported network states and notify Flutter of this new state
                int status;
                if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    status = Constants.WIFI;
                } else if (netCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    status = Constants.CELLULAR;
                } else {
                    status = Constants.UNKNOWN;
                }
                // уведомление Flutter о новом состоянии.
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(eventSink != null) {
                            eventSink.success(status);
                        }
                    }
                });
            }
        };
    }
}