package org.codarama.haxsync.provider.facebook;

public interface Status {
    String getMessage();

    long getTimestamp();

    String getPermalink();

    String getID();
}
