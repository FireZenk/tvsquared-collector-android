package com.tvsquared;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

public class TVSquaredCollector {

    private String hostname = null;
    private String siteid = null;
    private boolean secure = false;
    private String visitorid = null;

    private String userId = null;
    private Random random = new Random(new Date().getTime());
    private static final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= 14; /*Build.VERSION_CODES.ICE_CREAM_SANDWICH; */
    private HttpHost proxy = null;

    public TVSquaredCollector(Activity activity, String hostname, String siteid) throws NoSuchAlgorithmException {
        this(activity, hostname, siteid, false);
    }

    public TVSquaredCollector(Activity activity, String hostname, String siteid, boolean secure) throws NoSuchAlgorithmException {
        this.hostname = hostname;
        this.siteid = siteid;
        this.secure = secure;

        this.visitorid = this.getVisitorId(activity);
        this.proxy = this.getProxy(activity);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void track() {
        this.track(null, null, null, 0, null);
    }

    public void track(String actionname, String product, String promocode, float revenue, String orderid) {
        new Thread(new AsyncTrack(this, actionname, product, promocode, revenue, orderid)).start();
    }

    private String getVisitorId(Activity activity)
            throws NoSuchAlgorithmException {
        String prefname = "visitor" + this.siteid;

        SharedPreferences settings = activity.getSharedPreferences("TVSquaredTracker", 0);
        String visitor = settings.getString(prefname, null);
        if (visitor == null) {
            visitor = this.md5(UUID.randomUUID().toString()).substring(0, 16);
            settings.edit().putString(prefname, visitor).commit();
        }
        return visitor;
    }

    private void appendSessionDetails(Uri.Builder builder)
            throws JSONException {
        JSONObject v5 = new JSONObject();
        v5.put("medium", "app");
        v5.put("dev", "android");
        if (this.userId != null)
            v5.put("user", this.userId);

        JSONArray custom5 = new JSONArray();
        custom5.put("session");
        custom5.put(v5.toString());

        JSONObject cvar = new JSONObject();
        cvar.put("5", custom5);

        builder.appendQueryParameter("_cvar", cvar.toString());
    }

    private void appendActionDetails(Uri.Builder builder, String actionname,
                                     String product, String promocode, float revenue, String orderid)
                                             throws JSONException {
        JSONObject v5 = new JSONObject();
        v5.put("prod", product);
        v5.put("promo", promocode);
        v5.put("rev", revenue);
        v5.put("id", orderid);

        JSONArray custom5 = new JSONArray();
        custom5.put(actionname);
        custom5.put(v5.toString());

        JSONObject cvar = new JSONObject();
        cvar.put("5", custom5);

        builder.appendQueryParameter("cvar", cvar.toString());
    }

    private String md5(final String s)
            throws NoSuchAlgorithmException {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }

    private HttpHost getProxy(Context context) {
        String proxyAddress;
        int proxyPort;

        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty( "http.proxyHost" );

            String portStr = System.getProperty( "http.proxyPort" );
            proxyPort = Integer.parseInt( ( portStr != null ? portStr : "-1" ) );
        } else {
            proxyAddress = android.net.Proxy.getHost( context );
            proxyPort = android.net.Proxy.getPort( context );
        }

        if (proxyAddress == null)
            return null;
        return new HttpHost(proxyAddress, proxyPort);
    }

    class AsyncTrack implements Runnable {
        private TVSquaredCollector tracker;
        private String actionname;
        private String product;
        private String promocode;
        private float revenue;
        private String orderid;

        public AsyncTrack(TVSquaredCollector tracker, String actionname, String product, String orderid, float revenue, String promocode) {
            this.tracker = tracker;
            this.actionname = actionname;
            this.product = product;
            this.promocode = promocode;
            this.revenue = revenue;
            this.orderid = orderid;
        }

        public void run() {
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme(this.tracker.secure ? "https" : "http")
                        .authority(this.tracker.hostname)
                        .path("/piwik/piwik.php")
                        .appendQueryParameter("idsite", String.valueOf(this.tracker.siteid))
                        .appendQueryParameter("rec", "1")
                        .appendQueryParameter("rand", "" + String.valueOf(random.nextInt()))
                        .appendQueryParameter("_id", this.tracker.visitorid);
                this.tracker.appendSessionDetails(builder);
                if ((actionname != null) && (actionname.trim().length() > 0))
                    this.tracker.appendActionDetails(builder, actionname, product, promocode, revenue, orderid);

               HttpClient client = new DefaultHttpClient();
               if (this.tracker.proxy != null)
                   client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, this.tracker.proxy);
               HttpGet request = new HttpGet(builder.build().toString());
               request.setHeader("User-Agent", "TVSquared Android Collector Client 1.0");

               HttpResponse resp = client.execute(request);
               if (resp.getStatusLine().getStatusCode() != 200)
                   System.err.println("Failed to track request: " + resp.getStatusLine().toString());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
