package com.murphy.pokotalk.server;

import android.content.Context;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.murphy.pokotalk.R;

import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public abstract class ServerSocket {
    protected String serverURL;
    protected Socket mSocket;
    protected HashMap<String, Emitter.Listener> handlers;
    protected HashMap<String, ArrayList<ActivityCallback>> activityHandlers;

    public ServerSocket(String url) {
        handlers = new HashMap<>();
        activityHandlers = new HashMap<>();
        serverURL = url;
    }

    protected void createSocket(Context context) throws Exception {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(
                   context.getResources().openRawResource(R.raw.file)); // from file server.crt

            // Create a KeyStore containing the trusted CAs.
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);
            // Create a TrustManager that trusts the CAs in KeyStore.
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            // Create an SSLContext that uses the TrustManager.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            IO.setDefaultSSLContext(sslContext);
            IO.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    //TODO: Make this more restrictive
                    return true;
                }
            });
            IO.Options opts = new IO.Options();
            opts.sslContext = sslContext;
            opts.secure = true;
            opts.upgrade = true;
            opts.reconnection = true;
            opts.forceNew = true;
            mSocket = IO.socket(serverURL, opts);
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected void connect() {
        mSocket.connect();
    }

    /* Add activity callback method */
    public void attachActivityCallback(String name, ActivityCallback callback) {
        ArrayList<ActivityCallback> callbacks = activityHandlers.get(name);
        if (callbacks == null) {
            callbacks = new ArrayList<>();
            activityHandlers.put(name, callbacks);
        }
        callbacks.add(callback);
    }

    /* Remove activity callback method */
    public void detachActivityCallback(String name, ActivityCallback callback) {
        ArrayList<ActivityCallback> callbacks = activityHandlers.get(name);
        if (callbacks == null)
            return;
        callbacks.remove(callback);
    }

    /* Call activity callbacks at the end of Emitter.Listener run */
    protected void startActivityCallbacks(String name, Status status, HashMap<String, Object> data, Object... args) {
        ArrayList<ActivityCallback> callbacks = activityHandlers.get(name);
        if (callbacks == null)
            return;

        Iterator iter = callbacks.iterator();
        while (iter.hasNext()) {
            ActivityCallback callback = (ActivityCallback) iter.next();
            callback.setArgs(status, data, args);
            callback.run();
        }
    }
}
