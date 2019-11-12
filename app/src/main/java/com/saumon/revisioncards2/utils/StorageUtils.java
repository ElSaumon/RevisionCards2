package com.saumon.revisioncards2.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.activities.BaseActivity;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.saumon.revisioncards2.models.Grade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class StorageUtils {
    private static final int RC_STORAGE_WRITE_PERMS = 100;
    private static final String BACKUP_FILENAME = "backup.json";
    private static final String BACKUP_FOLDERNAME = "revisionCards";
    private static final int BACKUP_VERSION = 2;

    @NonNull
    private static File createOrGetFile(File destination, String fileName, String folderName) {
        File folder = new File(destination, folderName);
        return new File(folder, fileName);
    }

    private static boolean setTextInStorage(File rootDestination, String fileName, String folderName, String text) {
        File file = createOrGetFile(rootDestination, fileName, folderName);
        return writeOnFile(text, file);
    }

    private static String getTextFromStorage(File rootDestination, String fileName, String folderName) {
        File file = createOrGetFile(rootDestination, fileName, folderName);
        return readOnFile(file);
    }

    private static boolean writeOnFile(String text, @NonNull File file) {
        try {
            Objects.requireNonNull(file.getParentFile()).mkdirs();
            FileOutputStream fos = new FileOutputStream(file);

            try (Writer w = new BufferedWriter(new OutputStreamWriter(fos))) {
                w.write(text);
                w.flush();
                fos.getFD().sync();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static String readOnFile(@NonNull File file) {
        String result = "";
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                result = sb.toString();
            } catch (IOException ignored) {

            }
        }

        return result;
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static void requestBackupPermissions(Activity activity) {
        PermissionRequest request = new PermissionRequest.Builder(activity, RC_STORAGE_WRITE_PERMS, WRITE_EXTERNAL_STORAGE)
                .setRationale(R.string.Write_permissions_request_message)
                .setPositiveButtonText(R.string.Yes)
                .setNegativeButtonText(R.string.No)
                .build();
        EasyPermissions.requestPermissions(request);
    }

    public static boolean isBackupPermissionsPermanentlyDenied(Activity activity) {
        List<String> perms = new ArrayList<>();
        perms.add(WRITE_EXTERNAL_STORAGE);
        return EasyPermissions.somePermissionPermanentlyDenied(activity, perms);
    }

    public static void backup(Activity activity, BackupAsyncTask.Listeners callback) {
        if (!isExternalStorageWritable()) {
            Toast.makeText(activity, R.string.Backup_fail, Toast.LENGTH_LONG).show();
            return;
        }

        new BackupAsyncTask(activity, callback).execute();
    }

    public static void restore(Activity activity, RestoreAsyncTask.Listeners callback, boolean emptyDatabase) {
        if (!isExternalStorageReadable()) {
            Toast.makeText(activity, R.string.Restore_fail, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            String fileContent = getTextFromStorage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_FILENAME, BACKUP_FOLDERNAME);
            if (fileContent.isEmpty()) {
                Toast.makeText(activity, R.string.No_data_to_restore, Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject json = new JSONObject(fileContent);
            if (!json.has("version")) {
                Toast.makeText(activity, R.string.Restore_fail_no_version, Toast.LENGTH_LONG).show();
                return;
            }
            if (!json.has("folders") || !json.has("cards")) {
                Toast.makeText(activity, R.string.No_data_to_restore, Toast.LENGTH_LONG).show();
                return;
            }
            if (BACKUP_VERSION != json.getInt("version")) {
                Toast.makeText(activity, R.string.Restore_fail_wrong_version, Toast.LENGTH_LONG).show();
                return;
            }

            new RestoreAsyncTask(activity, callback, json, emptyDatabase).execute();
        } catch (JSONException e) {
            Toast.makeText(activity, R.string.Restore_fail, Toast.LENGTH_LONG).show();
        }
    }

    public static class BackupAsyncTask extends AsyncTask<Void, Void, Boolean> {
        public interface Listeners {
            void onPreExecuteBackup();
            void onPostExecuteBackup();
        }

        private final WeakReference<Activity> activity;
        private final WeakReference<Listeners> callback;
        private JSONObject json;

        BackupAsyncTask(Activity activity, Listeners callback) {
            this.activity = new WeakReference<>(activity);
            this.callback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.get().onPreExecuteBackup();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            json = new JSONObject();

            try {
                json.put("version", BACKUP_VERSION);
                addChildrenToJson(json, null);
            } catch (JSONException e) {
                return false;
            }

            return true;
        }

        private void addChildrenToJson(JSONObject parentJson, Long parentId) throws JSONException {
            CardViewModel cardViewModel = ((BaseActivity) activity.get()).cardViewModel;

            JSONArray foldersJson = new JSONArray();
            List<Folder> folderList;
            if (null == parentId) {
                folderList = cardViewModel.getParentFoldersSync();
            } else {
                folderList = cardViewModel.getFoldersByParentIdSync(parentId);
            }

            for (Folder folder : folderList) {
                JSONObject folderJson = new JSONObject();

                folderJson.put("creationDate", DateTypeConverter.toLong(folder.getCreationDate()));
                folderJson.put("name", folder.getName());

                addChildrenToJson(folderJson, folder.getId());

                foldersJson.put(folderJson);
            }

            if (!folderList.isEmpty()) {
                parentJson.put("folders", foldersJson);
            }

            JSONArray cardsJson = new JSONArray();
            List<Card> cardList;
            if (null == parentId) {
                cardList = cardViewModel.getCardsWithoutParentSync();
            } else {
                cardList = cardViewModel.getCardsByFolderIdSync(parentId);
            }

            for (Card card : cardList) {
                JSONObject cardJson = new JSONObject();

                cardJson.put("creationDate", DateTypeConverter.toLong(card.getCreationDate()));
                cardJson.put("name", card.getName());
                cardJson.put("text1", card.getText1());
                cardJson.put("text2", card.getText2());
                cardJson.put("sideToShow", card.getSideToShow());

                JSONArray gradesJson = new JSONArray();
                List<Grade> gradeList = cardViewModel.getGradesFromCardSync(card.getId());

                for (Grade grade : gradeList) {
                    JSONObject gradeJson = new JSONObject();

                    gradeJson.put("creationDate", DateTypeConverter.toLong(grade.getCreationDate()));
                    gradeJson.put("value", grade.getValue());

                    gradesJson.put(gradeJson);
                }

                if (!gradeList.isEmpty()) {
                    cardJson.put("grades", gradesJson);
                }

                cardsJson.put(cardJson);
            }

            if (!cardList.isEmpty()) {
                parentJson.put("cards", cardsJson);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            callback.get().onPostExecuteBackup();
            if (result) {
                if (setTextInStorage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), BACKUP_FILENAME, BACKUP_FOLDERNAME, json.toString())) {
                    Toast.makeText(activity.get(), R.string.Backup_success, Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Toast.makeText(activity.get(), R.string.Backup_fail, Toast.LENGTH_LONG).show();
        }
    }

    public static class RestoreAsyncTask extends AsyncTask<Void, Void, Boolean> {
        public interface Listeners {
            void onPreExecuteRestore();
            void onPostExecuteRestore();
        }

        private final WeakReference<Activity> activity;
        private final WeakReference<Listeners> callback;
        private final WeakReference<JSONObject> json;
        private final WeakReference<Boolean> emptyDatabase;

        RestoreAsyncTask(Activity activity, Listeners callback, JSONObject json, boolean emptyDatabase) {
            this.activity = new WeakReference<>(activity);
            this.callback = new WeakReference<>(callback);
            this.json = new WeakReference<>(json);
            this.emptyDatabase = new WeakReference<>(emptyDatabase);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callback.get().onPreExecuteRestore();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (emptyDatabase.get()) {
                DatabaseUtils.emptyDatabase(activity.get());
            }

            try {
                insertChildrenIntoDatabase(json.get(), null);
            } catch (JSONException e) {
                return false;
            }

            return true;
        }

        private void insertChildrenIntoDatabase(@NonNull JSONObject json, Long parentId) throws JSONException {
            CardViewModel cardViewModel = ((BaseActivity) activity.get()).cardViewModel;

            if (json.has("folders")) {
                JSONArray foldersJson = json.getJSONArray("folders");
                for (int i = 0; i < foldersJson.length(); i++) {
                    JSONObject folderJson = foldersJson.getJSONObject(i);
                    Folder folder = new Folder(DateTypeConverter.toDate(folderJson.getLong("creationDate")), parentId, folderJson.getString("name"));
                    cardViewModel.createFolderSync(folder);

                    insertChildrenIntoDatabase(folderJson, folder.getId());
                }
            }
            if (json.has("cards")) {
                JSONArray cardsJson = json.getJSONArray("cards");
                for (int ic = 0; ic < cardsJson.length(); ic++) {
                    JSONObject cardJson = cardsJson.getJSONObject(ic);
                    String cardName;
                    if (cardJson.has("name")) {
                        cardName = cardJson.getString("name");
                    } else {
                        cardName = null;
                    }
                    Card card = new Card(DateTypeConverter.toDate(cardJson.getLong("creationDate")), parentId, cardName, cardJson.getString("text1"), cardJson.getString("text2"), cardJson.getInt("sideToShow"));
                    cardViewModel.createCardSync(card);

                    if (cardJson.has("grades")) {
                        JSONArray gradesJson = cardJson.getJSONArray("grades");
                        for (int ig = 0; ig < gradesJson.length(); ig++) {
                            JSONObject gradeJson = gradesJson.getJSONObject(ig);
                            Grade grade = new Grade(DateTypeConverter.toDate(gradeJson.getLong("creationDate")), card.getId(), gradeJson.getInt("value"));
                            cardViewModel.createGradeSync(grade);
                        }
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            callback.get().onPostExecuteRestore();
            if (result) {
                Toast.makeText(activity.get(), R.string.Restore_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity.get(), R.string.Restore_fail, Toast.LENGTH_LONG).show();
            }
        }
    }
}
