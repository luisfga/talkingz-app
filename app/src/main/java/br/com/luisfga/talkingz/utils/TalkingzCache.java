package br.com.luisfga.talkingz.utils;

import android.util.LruCache;

public class TalkingzCache {

    private static TalkingzCache instance;

    public static final String BITMAP_ON_CACHE = "BITMAP_ON_CACHE";

    private LruCache<Object, Object> lru;

    private TalkingzCache() {
        lru = new LruCache<Object, Object>(1024);
    }

    public static TalkingzCache getInstance() {
        if (instance == null) {
            instance = new TalkingzCache();
        }
        return instance;
    }

    public LruCache<Object, Object> getLru() {
        return lru;
    }
}
