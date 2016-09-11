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

package org.codarama.haxsync.services;

import static org.junit.Assert.assertEquals;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import org.codarama.haxsync.BuildConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboSharedPreferences;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * See http://robolectric.org/writing-a-test/
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk= 21)
public class ContactSyncAdapterTest {

//    @Before
//    public void init() {
//        Account account = new Account("compte n°1", GoogleAccountManager.ACCOUNT_TYPE);
//        AccountManager accountManager = AccountManager.get(Robolectric.application);
//        shadowOf(accountManager).addAccount(account0);
//    }

    @Test
    public void convertWebcalAddressSuccess() throws MalformedURLException {
        Application application = RuntimeEnvironment.application;
        RoboSharedPreferences preferences = (RoboSharedPreferences) application
                .getSharedPreferences("example", Context.MODE_PRIVATE);
        Intent intent = new Intent(application, ContactSyncService.class);
        ContactSyncAdapter contactSyncAdapter = new ContactSyncAdapter(application.getApplicationContext(), true);

        URL url = contactSyncAdapter.processAddress("webcal://www.facebook.com/ical/b.php?uid=123456789&key=fgTTr56dDSs");

        assertEquals("http://www.facebook.com/ical/b.php?uid=123456789&key=fgTTr56dDSs", url.toString());
    }

    @Test
    public void noConvertionOfAddressSuccess() throws MalformedURLException {
        Application application = RuntimeEnvironment.application;
        RoboSharedPreferences preferences = (RoboSharedPreferences) application
                .getSharedPreferences("example", Context.MODE_PRIVATE);
        Intent intent = new Intent(application, ContactSyncService.class);
        ContactSyncAdapter contactSyncAdapter = new ContactSyncAdapter(application.getApplicationContext(), true);

        URL url = contactSyncAdapter.processAddress("http://www.facebook.com/ical/b.php?uid=123456789&key=fgTTr56dDSs");

        assertEquals("http://www.facebook.com/ical/b.php?uid=123456789&key=fgTTr56dDSs", url.toString());
    }

    @Test(expected = MalformedURLException.class)
    public void conversionFailure() throws MalformedURLException {
        Application application = RuntimeEnvironment.application;
        RoboSharedPreferences preferences = (RoboSharedPreferences) application
                .getSharedPreferences("example", Context.MODE_PRIVATE);
        Intent intent = new Intent(application, ContactSyncService.class);
        ContactSyncAdapter contactSyncAdapter = new ContactSyncAdapter(application.getApplicationContext(), true);

        URL url = contactSyncAdapter.processAddress("!@#$%^&*(thisisobviouslynotavalidaddress!@#$%^&*");
    }
}
