package com.saumon.revisioncards2.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.utils.FillDatabaseAsyncTask;
import com.saumon.revisioncards2.utils.StorageUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, StorageUtils.BackupAsyncTask.Listeners, StorageUtils.RestoreAsyncTask.Listeners, FillDatabaseAsyncTask.Listeners {
    @BindView(R.id.activity_home_fill_database_btn) Button fillDatabaseButton;
    @BindView(R.id.activity_home_backup_btn) Button backupButton;
    @BindView(R.id.activity_home_restore_btn) Button restoreButton;
    @BindView(R.id.activity_home_message_txt) TextView messageTextView;

    private boolean hasClickBackupButton = false;
    private boolean hasClickRestoreButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        if (!res.getBoolean(R.bool.DEBUG)) {
            fillDatabaseButton.setVisibility(View.GONE);
        }
        if (!EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)) {
            StorageUtils.requestBackupPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (hasClickBackupButton) {
            hasClickBackupButton = false;
            StorageUtils.backup(this, this);
        }
        if (hasClickRestoreButton) {
            hasClickRestoreButton = false;
            askEmptyDatabaseRestore();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (StorageUtils.isBackupPermissionsPermanentlyDenied(this)) {
            backupButton.setVisibility(View.GONE);
            restoreButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutContentViewID() {
        return R.layout.activity_home;
    }

    @Override
    protected void configureToolbar() {
        Toolbar toolbar = getToolbar();
        toolbar.setTitle(getToolbarTitle());
        setSupportActionBar(toolbar);
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.Home);
    }

    @OnClick(R.id.activity_home_review_btn)
    public void onClickReviewButton() {
       Intent intent = new Intent(this, CardsRevisionSelectorActivity.class);
       startActivity(intent);
    }

    @OnClick(R.id.activity_home_cards_manage_btn)
    public void onClickCardsManageButton() {
        Intent intent = new Intent(this, CardsManagerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.activity_home_backup_btn)
    public void onClickBackupButton() {
        if (!EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)) {
            hasClickBackupButton = true;
            StorageUtils.requestBackupPermissions(this);
            return;
        }
        StorageUtils.backup(this, this);
    }

    @OnClick(R.id.activity_home_restore_btn)
    public void onClickRestoreButton() {
        if (!EasyPermissions.hasPermissions(this, WRITE_EXTERNAL_STORAGE)) {
            hasClickRestoreButton = true;
            StorageUtils.requestBackupPermissions(this);
            return;
        }
        askEmptyDatabaseRestore();
    }

    @OnClick(R.id.activity_home_fill_database_btn)
    public void onClickFillDatabaseButton() {
        new FillDatabaseAsyncTask(this, this).execute();
    }

    private void askEmptyDatabaseRestore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Overwrite_data))
                .setMessage(getString(R.string.Overwrite_data_confirmation))
                .setNegativeButton(getString(R.string.No), (dialogInterface, i) -> StorageUtils.restore(this, this, false))
                .setPositiveButton(getString(R.string.Yes), (dialogInterface, i) -> StorageUtils.restore(this, this, true))
                .create()
                .show();
    }

    @Override
    public void onPreExecuteBackup() {
        backupButton.setEnabled(false);
        messageTextView.setText(getString(R.string.Backup_running));
    }

    @Override
    public void onPostExecuteBackup() {
        messageTextView.setText("");
        backupButton.setEnabled(true);
    }

    @Override
    public void onPreExecuteRestore() {
        restoreButton.setEnabled(false);
        messageTextView.setText(getString(R.string.Restore_running));
    }

    @Override
    public void onPostExecuteRestore() {
        messageTextView.setText("");
        restoreButton.setEnabled(true);
    }

    @Override
    public void onPreExecuteFillDatabase() {
        fillDatabaseButton.setEnabled(false);
        messageTextView.setText(getString(R.string.Filling_database));
    }

    @Override
    public void onPostExecuteFillDatabase() {
        messageTextView.setText("");
        fillDatabaseButton.setEnabled(true);
        Toast.makeText(this, R.string.Fill_database_end, Toast.LENGTH_LONG).show();
    }
}
