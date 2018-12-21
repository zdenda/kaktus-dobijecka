package eu.zkkn.android.kaktus.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Define an implementation of ContentProvider that stubs out all methods
 *
 * http://developer.android.com/training/sync-adapters/creating-stub-provider.html
 */
public class StubProvider extends ContentProvider {

    // Always return true, indicating that the provider loaded correctly.
    @Override
    public boolean onCreate() {
        return true;
    }

    // Return no type for MIME type
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

     // query() always returns no results
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        return null;
    }

    // insert() always returns null (no URI)
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    //delete() always returns "no rows affected" (0)
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    // update() always returns "no rows affected" (0)
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        return 0;
    }

}
