/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.rittz.hackathon.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * A persistent cookie store which implements the Apache HttpClient
 * {@link CookieStore} interface. Cookies are stored and will persist on the
 * user's device between application sessions since they are serialized and
 * stored in {@link SharedPreferences}.
 * <p>
 * Instances of this class are designed to be used with
 * {@link AsyncHttpClient#setCookieStore}, but can also be used with a
 * regular old apache HttpClient/HttpContext if you prefer.
 */
public class PersistentCookieStore implements CookieStore {

    public static PersistentCookieStore instance;

    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String COOKIE_NAME_STORE = "names";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final ConcurrentHashMap<String, Cookie> cookies;
    private final SharedPreferences cookiePrefs;

    public static synchronized PersistentCookieStore getInstance(final Application context) {
        if (instance == null) {
            instance = new PersistentCookieStore(context);
        }
        return instance;
    }

    /**
     * Construct a persistent cookie store.
     */
    private PersistentCookieStore(final Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        cookies = new ConcurrentHashMap<String, Cookie>();

        // Load any previously stored cookies into the store
        final String storedCookieNames = cookiePrefs.getString(COOKIE_NAME_STORE, null);
        if(storedCookieNames != null) {
            final String[] cookieNames = TextUtils.split(storedCookieNames, ",");
            for(final String name : cookieNames) {
                final String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                if(encodedCookie != null) {
                    final Cookie decodedCookie = decodeCookie(encodedCookie);
                    if(decodedCookie != null) {
                        cookies.put(name, decodedCookie);
                    }
                }
            }

            // Clear out expired cookies
            clearExpired(new Date());
        }
    }

    @Override
    public void addCookie(final Cookie cookie) {
        final String name = cookie.getName() + cookie.getDomain();

        // Save cookie into local store, or remove if expired
        if(!cookie.isExpired(new Date())) {
            cookies.put(name, cookie);
        } else {
            cookies.remove(name);
        }

        // Save cookie into persistent store
        final SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", cookies.keySet()));
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableCookie(cookie)));
        prefsWriter.commit();
    }

    @Override
    public void clear() {
        // Clear cookies from local store
        cookies.clear();

        // Clear cookies from persistent store
        final SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        for(final String name : cookies.keySet()) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }
        prefsWriter.remove(COOKIE_NAME_STORE);
        prefsWriter.commit();
    }

    @Override
    public boolean clearExpired(final Date date) {
        boolean clearedAny = false;
        final SharedPreferences.Editor prefsWriter = cookiePrefs.edit();

        for(final ConcurrentHashMap.Entry<String, Cookie> entry : cookies.entrySet()) {
            final String name = entry.getKey();
            final Cookie cookie = entry.getValue();
            if(cookie.isExpired(date)) {
                // Clear cookies from local store
                cookies.remove(name);

                // Clear cookies from persistent store
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);

                // We've cleared at least one
                clearedAny = true;
            }
        }

        // Update names in persistent store
        if(clearedAny) {
            prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", cookies.keySet()));
        }
        prefsWriter.commit();

        return clearedAny;
    }

    @Override
    public List<Cookie> getCookies() {
        return new ArrayList<Cookie>(cookies.values());
    }


    //
    // Cookie serialization/deserialization
    //

    protected String encodeCookie(final SerializableCookie cookie) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (final Exception e) {
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    protected Cookie decodeCookie(final String cookieStr) {
        final byte[] bytes = hexStringToByteArray(cookieStr);
        final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
           final ObjectInputStream ois = new ObjectInputStream(is);
           cookie = ((SerializableCookie)ois.readObject()).getCookie();
        } catch (final Exception e) {
           e.printStackTrace();
        }

        return cookie;
    }

    // Using some super basic byte array <-> hex conversions so we don't have
    // to rely on any large Base64 libraries. Can be overridden if you like!
    protected String byteArrayToHexString(final byte[] b) {
        final StringBuffer sb = new StringBuffer(b.length * 2);
        for (final byte element : b) {
            final int v = element & 0xff;
            if(v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    protected byte[] hexStringToByteArray(final String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for(int i=0; i<len; i+=2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}