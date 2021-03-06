package com.tvsquared.sample;

import java.security.NoSuchAlgorithmException;

import com.tvsquared.TVSquaredCollector;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class SampleActivity extends Activity {

    private TVSquaredCollector tracker = null;
    private EditText txt_userid;
    private EditText txt_actionname;
    private EditText txt_product;
    private EditText txt_orderid;
    private EditText txt_revenue;
    private EditText txt_promocode;

    public SampleActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TVSquaredCollector.getInstance().init(this, "<COLLECTORHOSTNAME>", "<COLLECTORSITEID>");

        setContentView(R.layout.sample);
        txt_userid = (EditText)findViewById(R.id.txt_userid);
        txt_actionname = (EditText)findViewById(R.id.txt_actionname);
        txt_product = (EditText)findViewById(R.id.txt_product);
        txt_orderid = (EditText)findViewById(R.id.txt_orderid);
        txt_revenue = (EditText)findViewById(R.id.txt_revenue);
        txt_promocode = (EditText)findViewById(R.id.txt_promocode);
    }

    public void trackSimple(View view) {
        TVSquaredCollector.getInstance().track();
    }

    public void trackUser(View view) {
        TVSquaredCollector.getInstance().setUserId(txt_userid.getText().toString());
        TVSquaredCollector.getInstance().track();
    }

    public void trackAction(View view) {
        TVSquaredCollector.getInstance().setUserId(txt_userid.getText().toString());
        TVSquaredCollector.getInstance().track(txt_actionname.getText().toString(),
                            txt_product.getText().toString(),
                            txt_orderid.getText().toString(),
                            Float.parseFloat(txt_revenue.getText().toString()),
                            txt_promocode.getText().toString());
    }
}
