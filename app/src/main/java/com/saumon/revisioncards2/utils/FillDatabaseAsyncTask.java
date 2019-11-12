package com.saumon.revisioncards2.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class FillDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {
    public interface Listeners {
        void onPreExecuteFillDatabase();
        void onPostExecuteFillDatabase();
    }

    private final WeakReference<Context> context;
    private final WeakReference<Listeners> callback;

    public FillDatabaseAsyncTask(Context context, Listeners callback) {
        this.context = new WeakReference<>(context);
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.get().onPreExecuteFillDatabase();
    }

    @Override
    protected Void doInBackground(Void... params) {
        DatabaseUtils.fillDatabase(context.get());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        callback.get().onPostExecuteFillDatabase();
    }
}
