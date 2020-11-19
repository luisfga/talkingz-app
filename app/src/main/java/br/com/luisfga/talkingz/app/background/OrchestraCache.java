package br.com.luisfga.talkingz.app.background;

import android.util.LruCache;

public class OrchestraCache {

    private static OrchestraCache instance;

    public static final String BITMAP_ON_CACHE = "BITMAP_ON_CACHE";

    private LruCache<Object, Object> lru;

    private OrchestraCache() {
        lru = new LruCache<Object, Object>(1024);
    }

    public static OrchestraCache getInstance() {
        if (instance == null) {
            instance = new OrchestraCache();
        }
        return instance;
    }

    public LruCache<Object, Object> getLru() {
        return lru;
    }
}
