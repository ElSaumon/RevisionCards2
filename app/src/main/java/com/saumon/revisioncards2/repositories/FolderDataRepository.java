package com.saumon.revisioncards2.repositories;

import androidx.lifecycle.LiveData;

import com.saumon.revisioncards2.database.dao.FolderDao;
import com.saumon.revisioncards2.models.Folder;

import java.util.List;

public class FolderDataRepository {
    private final FolderDao folderDao;

    public FolderDataRepository(FolderDao folderDao) {
        this.folderDao = folderDao;
    }

    public LiveData<List<Folder>> getFolders() {
        return folderDao.getFolders();
    }

    public LiveData<List<Folder>> getParentFolders() {
        return folderDao.getParentFolders();
    }

    public List<Folder> getParentFoldersSync() {
        return folderDao.getParentFoldersSync();
    }

    public LiveData<List<Folder>> getFoldersByParentId(long parentId) {
        return folderDao.getFoldersByParentId(parentId);
    }

    public List<Folder> getFoldersByParentIdSync(long parentId) {
        return folderDao.getFoldersByParentIdSync(parentId);
    }

    public void createFolder(Folder folder) {
        long folderId = folderDao.insertFolder(folder);
        folder.setId(folderId);
    }

    public void updateFolder(Folder folder) {
        folderDao.updateFolder(folder);
    }

    public void deleteFolder(Folder folder) {
        folderDao.deleteFolder(folder);
    }
}
