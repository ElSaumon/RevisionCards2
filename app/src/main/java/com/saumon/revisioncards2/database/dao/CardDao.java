package com.saumon.revisioncards2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.saumon.revisioncards2.models.Card;

import java.util.List;

@Dao
public interface CardDao {
    @Query("SELECT * FROM Card ORDER BY folderId ASC, creationDate ASC")
    LiveData<List<Card>> getCards();

    @Query("SELECT * FROM Card WHERE folderId IS NULL ORDER BY creationDate ASC")
    LiveData<List<Card>> getCardsWithoutParent();

    @Query("SELECT * FROM Card WHERE folderId IS NULL ORDER BY creationDate ASC")
    List<Card> getCardsWithoutParentSync();

    @Query("SELECT * FROM Card WHERE folderId = :folderId ORDER BY creationDate ASC")
    LiveData<List<Card>> getCardsByFolderId(long folderId);

    @Query("SELECT * FROM Card WHERE folderId = :folderId ORDER BY creationDate ASC")
    List<Card> getCardsByFolderIdSync(long folderId);

    @Insert
    long insertCard(Card card);

    @Update
    void updateCard(Card card);

    @Delete
    void deleteCard(Card card);
}
