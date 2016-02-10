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

package org.codarama.haxsync.utilities.intents;

import android.app.Activity;

import java.util.List;

/**
 * <p>High amount of magic happens here.</p>
 *
 * <p>This is a missing class from the original repo that I will try to recreate. Supposedly it should be used to build up Intents<p/>
 */
public class IntentUtil {

    public static IntentBuilder getIntentBuilder(Activity activity) {
        throw new RuntimeException("Not yet developed");
    }

    public static IntentBuilder getFallbackBuilder() {
        throw new RuntimeException("Not yet developed");
    }

    public static NameList getApps(Activity activity) {
        throw new RuntimeException("Not yet developed");
    }

    public static class NameList {
        public List<Character> namesAvail;
        public List<Character> pkgsAvail;
    }
}
