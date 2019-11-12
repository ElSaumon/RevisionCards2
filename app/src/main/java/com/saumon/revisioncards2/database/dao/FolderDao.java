package com.saumon.revisioncards2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.saumon.revisioncards2.models.Folder;

import java.util.List;

@Dao
public interface FolderDao {
    @Query("SELECT * FROM Folder ORDER BY parentId ASC, creationDate ASC")
    LiveData<List<Folder>> getFolders();

    @Query("SELECT * FROM Folder WHERE parentId IS NULL ORDER BY creationDate ASC")
    LiveData<List<Folder>> getParentFolders();

    @Query("SELECT * FROM Folder WHERE parentId IS NULL ORDER BY creationDate ASC")
    List<Folder> getParentFoldersSync();

    @Query("SELECT * FROM Folder WHERE parentId = :parentId ORDER BY creationDate ASC")
    LiveData<List<Folder>> getFoldersByParentId(long parentId);

    @Query("SELECT * FROM Folder WHERE parentId = :parentId ORDER BY creationDate ASC")
    List<Folder> getFoldersByParentIdSync(long parentId);

    @Insert
    long insertFolder(Folder folder);

    @Update
    void updateFolder(Folder folder);

    @Delete
    void deleteFolder(Folder folder);
}
