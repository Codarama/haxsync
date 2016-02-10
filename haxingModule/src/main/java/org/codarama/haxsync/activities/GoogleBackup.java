/*
 * Copyright (c) 2016 Codarama.org, All Rights Reserved
 *
 * Codarama HaxSync is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * Codarama HaxSync is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.codarama.haxsync.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.codarama.haxsync.R;
import org.codarama.haxsync.contacts.ContactsService;
import org.codarama.haxsync.utilities.FacebookUtil;


public class GoogleBackup extends Activity {

    private SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_backup);
        AccountManager am = AccountManager.get(this);
        final Account[] googleAccounts = am.getAccountsByType("com.google");
        if (googleAccounts.length > 1) {
            final CharSequence[] items = new CharSequence[googleAccounts.length];
            for (int i = 0; i < googleAccounts.length; i++) {
                items[i] = googleAccounts[i].name;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(this.getString(R.string.google_select));
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    googleAccounts[0] = googleAccounts[item];
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        final Account googleAcc = googleAccounts[0];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.google_backup_warning))
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        GoogleBackup.this.finish();
                    }
                })
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new FriendWorker(GoogleBackup.this, googleAcc).execute();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GoogleBackup.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    final class FriendWorker extends AsyncTask<Void, Integer, Boolean> {
        private final GoogleBackup parent;
        private final Account googleAcc;
        private String contactName = "";

        protected FriendWorker(final GoogleBackup parent, final Account googleAcc) {
            this.parent = parent;
            this.googleAcc = googleAcc;
        }

        protected Boolean doInBackground(Void... params) {
            AccountManager am = AccountManager.get(parent);
            Account account = am.getAccountsByType(parent.getString(R.string.ACCOUNT_TYPE))[0];

            if (FacebookUtil.authorize(parent, account)) {
                String selfID = FacebookUtil.getSelfID();
                if (selfID == null) {
                    return false;
                }
                String googleName = googleAcc.name;
                Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
                        .appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
                        .appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
                        .build();
                Uri googleUri = RawContacts.CONTENT_URI.buildUpon()
                        .appendQueryParameter(RawContacts.ACCOUNT_NAME, googleName)
                        .appendQueryParameter(RawContacts.ACCOUNT_TYPE, "com.google")
                        .build();
                ContentResolver resolver = parent.getContentResolver();
                Cursor c1 = resolver.query(rawContactUri, new String[]{BaseColumns._ID, RawContacts.CONTACT_ID, RawContacts.DISPLAY_NAME_PRIMARY, RawContacts.SYNC1}, null, null, null);
                while (c1.moveToNext()) {
                    long contactID = c1.getLong(c1.getColumnIndex(RawContacts.CONTACT_ID));
                    Cursor c2 = resolver.query(googleUri, new String[]{BaseColumns._ID}, RawContacts.CONTACT_ID + " = '" + contactID + "'", null, null);
                    if (c2.getCount() > 0) {
                        c2.moveToFirst();
                        contactName = c1.getString(c1.getColumnIndex(RawContacts.DISPLAY_NAME_PRIMARY));
                        ContactsService manager = new ContactsService(resolver);
                        manager.writeHTCData(c2.getLong(c2.getColumnIndex(BaseColumns._ID)), selfID, c1.getString(c1.getColumnIndex(RawContacts.SYNC1)));
                        publishProgress((int) ((c1.getPosition() / (float) c1.getCount()) * 100));

                    }
                    c2.close();
                }
                c1.close();
                ContentResolver.requestSync(googleAcc, ContactsContract.AUTHORITY, new Bundle());
                return true;
            } else {
                return false;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            ProgressBar update = (ProgressBar) parent.findViewById(R.id.progressBar);
            TextView name = (TextView) parent.findViewById(R.id.contactName);
            name.setText(contactName);
            update.setProgress(progress[0]);
        }

        protected void onPostExecute(boolean result) {
            if (!result) {
                Toast toast = Toast.makeText(parent, "Error connecting to Facebook.", Toast.LENGTH_LONG);
                toast.show();
            }
            parent.finish();
        }
    }
}
