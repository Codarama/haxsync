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

package org.codarama.haxsync.utilities;

import android.util.Log;

import com.jjnford.android.util.Shell;
import com.jjnford.android.util.Shell.ShellException;

public class RootUtil {
    private static final String PICTURE_DIR = "/data/data/com.android.providers.contacts/files/photos/";

    public static boolean isRoot() {
        return Shell.su();
    }

    public static String movePic(String path, String file) throws ShellException {
        String newpath = PICTURE_DIR + file;
        String command = "mv " + path + " " + PICTURE_DIR + file;
        Log.i("COMMAND", command);
        Shell.sudo(command);
        return newpath;
    }

    public static void changeOwner(String file) throws ShellException {
        String command = "chown app_1.app_1 " + file;
        Log.i("COMMAND", command);
        Shell.sudo(command);
    }

    public static void refreshContacts() throws ShellException {
        Shell.sudo("pm disable com.android.providers.contacts");
        Shell.sudo("pm enable com.android.providers.contacts");
    }

    public static String listPics() throws ShellException {
        return Shell.sudo("ls " + PICTURE_DIR);
    }


}
