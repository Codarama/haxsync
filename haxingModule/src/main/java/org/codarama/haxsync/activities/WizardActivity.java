package org.codarama.haxsync.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.codarama.haxsync.R;
import org.codarama.haxsync.asynctasks.QuickSettings;
import org.codarama.haxsync.utilities.DeviceUtil;
import org.codarama.haxsync.utilities.FacebookUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * <p>Main configuration wizard<p/>
 * <p>Used for initial (when it is started for the first time) and generic (any time the user
 * decides to reconfigure the application) application set up</p>
 */
public class WizardActivity extends AccountAuthenticatorActivity {

    private final String TAG = "WizardActivity";
    private final String DEFAULT_USER_NAME = "John Doe"; // default user name when one could not be resolved normally
    private ArrayList<Integer> steps = new ArrayList<Integer>();
    private String[] permissions = {"offline_access", "read_stream", "user_events", "friends_events", "friends_status", "user_status",
            "friends_photos", "user_photos", "friends_about_me", "friends_website", "email", "friends_birthday", "friends_location"};
    private ViewFlipper flipper = null;
    private View next = null;
    private View settingsView = null;
    private TextView stepDisplay = null;
    private Spinner contactSpinner = null;

    // TODO Evaluate if removing the workaround code would have any adverse effects on the code
    //    private Button workaroundButton = null;
    private Switch eventSwitch;
    private Switch birthdaySwitch;
    private Switch reminderSwitch;
    private CheckBox wizardCheck;
    private ShowcaseView sv;
    private boolean isShowCase = false;
    private View showcaseView;
    private CallbackManager callbackManager;


    private void setupSteps() {

        // Step 1. Facebook login (shown only when there is no HaxSync account created)
        if (!DeviceUtil.hasAccount(this)) {
            steps.add(R.layout.wiz_fb_login);
            next.setEnabled(false);
        }

        // Step 1a. Workaround screen (DISABLED)
// TODO Evaluate if removing the workaround code would have any adverse effects on the code
//        if (DeviceUtil.needsWorkaround(this)) {
//            steps.add(R.layout.wiz_workaround);
//        }

        // Step 2. Existing settings
        if (shouldSkipSettings()) {
            steps.add(R.layout.wiz_existing_settings);
        }

        // Step 3. Settings view
        steps.add(R.layout.wiz_settings);

        // Step 4. Wizard completed
        steps.add(R.layout.wiz_success);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void toggleReminderVisibility() {
        if (birthdaySwitch.isChecked() || eventSwitch.isChecked()) {
            reminderSwitch.setVisibility(View.VISIBLE);
        } else {
            reminderSwitch.setVisibility(View.INVISIBLE);
        }
    }

    private boolean shouldSkipSettings() {
        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_MULTI_PROCESS);
        return ((prefs.getBoolean("sync_birthdays", true) != prefs.getBoolean("sync_contact_birthday", true)) || (prefs.getBoolean("birthday_reminders", true) != prefs.getBoolean("event_reminders", true)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_wizard);

        callbackManager = CallbackManager.Factory.create();

        flipper = (ViewFlipper) findViewById(R.id.wizardFlipper);
        next = findViewById(R.id.nextView);
        settingsView = findViewById(R.id.settingsView);

        setupSteps();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int step : steps) {
            View stepView = inflater.inflate(step, this.flipper, false);
            this.flipper.addView(stepView);
        }

        //restore active step after orientation change
        if (savedInstanceState != null) {
            int step = savedInstanceState.getInt("step");
            if (steps.contains(step))
                flipper.setDisplayedChild(steps.indexOf(step));
        }

        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

// TODO Evaluate if removing the workaround code would have any adverse effects on the code
//        workaroundButton = (Button) findViewById(R.id.workaroundButton);

        wizardCheck = (CheckBox) findViewById(R.id.checkHide);

        if (loginButton != null) {
            loginButton.setReadPermissions("user_friends");
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    final AccessToken token = loginResult.getAccessToken();
                    Log.i(TAG, "Facebook login successful");
                    Log.i(TAG, "Facebook token expires on " + token.getExpires().toString());
                    final AccountManager am = AccountManager.get(WizardActivity.this);

                    if (!DeviceUtil.hasAccount(WizardActivity.this)) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                token,
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        Log.i(TAG, "Received Facebook response");
                                        Log.i(TAG, object.toString());

                                        Bundle result;
                                        String accountName = DEFAULT_USER_NAME;
                                        try {
                                            accountName = object.getString("name");
                                        } catch (JSONException e) {
                                            Log.e(TAG, "Failed to parse name of logged in user", e);
                                        }
                                        String accountType = WizardActivity.this.getString((R.string.ACCOUNT_TYPE));
                                        Account account = new Account(accountName, accountType);
                                        if (am.addAccountExplicitly(account, token.toString(), null)) {
                                            result = new Bundle();
                                            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                                            setAccountAuthenticatorResult(result);

                                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WizardActivity.this);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putInt("permission_level", FacebookUtil.PERMISSION_LEVEL);
                                            editor.putLong("access_expires", token.getExpires().getTime());
                                            editor.commit();

                                            next.setEnabled(true);
                                            flipper.showNext();
                                            updateNextView();

                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "name, picture");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }
                }

                @Override
                public void onCancel() {
                    Log.w(TAG, "Facebook login canceled");
                }

                @Override
                public void onError(FacebookException exception) {
                    Log.e(TAG, "Facebook login failed", exception);
                }
            });
        }

// TODO Evaluate if removing the workaround code would have any adverse effects on the code
//        if (workaroundButton != null) {
//            workaroundButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse("market://details?id=com.haxsync.facebook.workaround"));
//                    startActivity(intent);
//                }
//            });
//        }

        eventSwitch = ((Switch) findViewById(R.id.switchEvent));
        birthdaySwitch = ((Switch) findViewById(R.id.switchBirthdays));

        // :(
        if (FacebookUtil.RESPECT_FACEBOOK_POLICY) {
            birthdaySwitch.setVisibility(View.GONE);
            findViewById(R.id.seperatorBirthdays).setVisibility(View.GONE);
        }

        reminderSwitch = ((Switch) findViewById(R.id.switchReminders));


        contactSpinner = (Spinner) findViewById(R.id.contactSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ContactsChoices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contactSpinner.setAdapter(adapter);

        readSettings();

        eventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleReminderVisibility();
            }
        });
        birthdaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleReminderVisibility();
            }
        });
        stepDisplay = (TextView) findViewById(R.id.stepView);
        updateNextView();

        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "child " + flipper.getDisplayedChild() + " of " + flipper.getChildCount());
                if (shouldSkipNext()) {
                    flipper.showNext();
                } else if (isSettings()) {
                    SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_MULTI_PROCESS);
                    boolean settingsFound = prefs.getBoolean("settings_found", false);

                    //highlight the settings button in case the user hasn't found the settings yet.
                    if (!settingsFound) {
                        showCaseSettings();
                    }
                    applySettings();
                } else if (isShowCase) {
                    sv.hide();
                    isShowCase = false;
                    SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_MULTI_PROCESS);
                    boolean settingsFound = prefs.getBoolean("settings_found", false);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("settings_found", true);
                    editor.commit();
                    //flipper.removeView(showcaseView);
                    //flipper.removeViewAt(flipper.getDisplayedChild());
                }
                if (isLast()) {
                    DeviceUtil.toggleWizard(WizardActivity.this, !wizardCheck.isChecked());
                    WizardActivity.this.setResult(Activity.RESULT_OK);
                    WizardActivity.this.finish();
                } else {
                    flipper.showNext();
                    //showcase doesn't count as step, so remove it from flipper so the step counter doesn't get messed up.
                    /*if (isShowCase){
                        flipper.removeViewAt(flipper.getDisplayedChild() - 1);
                        isShowCase = false;
                    }*/

                    updateNextView();


                }
            }
        });

        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(WizardActivity.this, Preferences.class);
                startActivity(i);
            }
        });
    }

    private void showCaseSettings() {
        //add empty view so showcase isn't fugly
        showcaseView = new View(this);
        flipper.addView(showcaseView, flipper.getDisplayedChild() + 1);
        isShowCase = true;
        //add -1 to steps array so things don't get confusing
        steps.add(steps.indexOf(R.layout.wiz_settings) + 1, -1);

        sv = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(settingsView))
                .setContentTitle(R.string.preferences)
                .setContentText(R.string.preferences_summary)
                .build();
    }


    private void readSettings() {
        Account account = DeviceUtil.getAccount(this);
        boolean contactSync = true;
        boolean calendarSync = true;
        if (account != null) {
            contactSync = ContentResolver.getSyncAutomatically(account, ContactsContract.AUTHORITY);
            calendarSync = ContentResolver.getSyncAutomatically(account, CalendarContract.AUTHORITY);
        }
        SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", MODE_MULTI_PROCESS);

        if (!contactSync)
            contactSpinner.setSelection(0);
        else if (prefs.getBoolean("phone_only", true))
            contactSpinner.setSelection(1);
        else
            contactSpinner.setSelection(2);

        eventSwitch.setChecked(prefs.getBoolean("sync_events", true) && calendarSync);
        birthdaySwitch.setChecked(prefs.getBoolean("sync_birthdays", true) && calendarSync);
        reminderSwitch.setChecked(prefs.getBoolean("event_reminders", true));
        wizardCheck.setChecked(!DeviceUtil.isWizardShown(this));
        toggleReminderVisibility();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        int step = steps.get(flipper.getDisplayedChild());
        savedInstanceState.putInt("step", step);
    }


    private void applySettings() {
        int contactChoice = contactSpinner.getSelectedItemPosition();
        boolean eventSync = eventSwitch.isChecked();
        boolean birthdaySync = birthdaySwitch.isChecked();
        boolean reminders = reminderSwitch.isChecked();

        new QuickSettings(this, contactChoice, eventSync, birthdaySync, reminders).execute();

    }

    private boolean isLast() {
        return (flipper.getDisplayedChild() + 1 == flipper.getChildCount()) && !isShowCase;
    }

    private boolean shouldSkipNext() {
        if (steps.get(flipper.getDisplayedChild()) == R.layout.wiz_existing_settings) {
            return ((RadioButton) findViewById(R.id.radioSkip)).isChecked();
        }
        return false;
    }

    private boolean isSettings() {
        return steps.get(flipper.getDisplayedChild()) == R.layout.wiz_settings;
    }

    private void updateNextView() {
        if (stepDisplay != null) {
            stepDisplay.setText(getResources().getString(R.string.step, flipper.getDisplayedChild() + 1, flipper.getChildCount()));
        }
//        if (isLast()) {
//            ((TextView) findViewById(R.id.nextLabel)).setText(getResources().getString(R.string.done));
//        }
    }


}
