package org.codarama.haxsync.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.facebook.appevents.AppEventsLogger;

import org.codarama.haxsync.R;

/**
 * <p>Welcome screen</p>
 * <p>Displayed when the application is started for the first time. Leads to the {@link WizardActivity}.</p>
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        ImageView image = (ImageView) findViewById(R.id.logo);
        image.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(WelcomeActivity.this, WizardActivity.class);
                WelcomeActivity.this.startActivity(nextIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                WelcomeActivity.this.finish();
            }
        });
    }
}