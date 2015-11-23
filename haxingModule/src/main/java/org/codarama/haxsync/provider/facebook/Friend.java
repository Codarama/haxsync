package org.codarama.haxsync.provider.facebook;

import java.util.ArrayList;


public interface Friend {
    String getName(boolean ignoreMiddleNames);

    String getUserName();

    String getPicURL();

    long getPicTimestamp();

    ArrayList<Status> getStatuses();
}
