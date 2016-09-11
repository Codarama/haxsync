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

package org.codarama.haxsync.contacts;

import org.codarama.haxsync.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

/**
 * See http://robolectric.org/writing-a-test/
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk= 21)
public class CalendarEntriesTest {

    @Test
    public void testNameDiscoveryWithThreeRandom() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Birthday of Darth Vader");
        parser.add("2", "12.12.1212", "Birthday of Luke Skywalker");
        parser.add("3", "12.12.1212", "Birthday of Darth Mol");

        assertEquals(parser.getContacts().get(0).getName(), "Darth Vader");
        assertEquals(parser.getContacts().get(1).getName(), "Luke Skywalker");
        assertEquals(parser.getContacts().get(2).getName(), "Darth Mol");
    }

    @Test
    public void testNameDiscoveryWithThree() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Birthday of Darth Vader");
        parser.add("2", "12.12.1212", "Birthday of Darth Mol");
        parser.add("3", "12.12.1212", "Birthday of Luke Skywalker");

        assertEquals(parser.getContacts().get(0).getName(), "Darth Vader");
        assertEquals(parser.getContacts().get(1).getName(), "Darth Mol");
        assertEquals(parser.getContacts().get(2).getName(), "Luke Skywalker");
    }

    @Test
    public void testNameDiscoveryWithTwo() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Birthday of Mara Jade");
        parser.add("2", "12.12.1212", "Birthday of Han Solo");

        assertEquals(parser.getContacts().get(0).getName(), "Mara Jade");
        assertEquals(parser.getContacts().get(1).getName(), "Han Solo");
    }

    @Test
    // FIXME known issue
    // no easy way to destinguish the name in case only two contacts are present and they both
    // start with the same name
    public void testNameDiscoveryWithTwoIdentical() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Birthday of Darth Vader");
        parser.add("2", "12.12.1212", "Birthday of Darth Mol");

        assertEquals(parser.getContacts().get(0).getName(), "Vader");
        assertEquals(parser.getContacts().get(1).getName(), "Mol");
    }

    @Test
    // FIXME known issue
    // if in some language the "Birthday of" part is a suffix we fail again
    public void testNameDiscoveryWithSuffix() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Mara Jade's birthday");
        parser.add("2", "12.12.1212", "Luke Skywalker's birthday");

        assertEquals(parser.getContacts().get(0).getName(), "Mara Jade's birthday");
        assertEquals(parser.getContacts().get(1).getName(), "Luke Skywalker's birthday");
    }

    @Test(expected = IllegalStateException.class)
    public void testNameDiscoveryWithOne() throws MalformedURLException {
        CalendarEntries parser = new CalendarEntries();

        parser.add("1", "12.12.1212", "Birthday of Mara Jade");

        assertEquals(parser.getContacts().get(0).getName(), "Mara Jade");
    }
}
