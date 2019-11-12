package com.saumon.revisioncards2.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.saumon.revisioncards2.models.Grade;

import java.util.List;

@Dao
public interface GradeDao {
    @Query("SELECT * FROM Grade ORDER BY cardId ASC, creationDate ASC")
    LiveData<List<Grade>> getGrades();

    @Query("SELECT * FROM Grade WHERE cardId = :cardId ORDER BY creationDate ASC")
    List<Grade> getGradesFromCard(long cardId);

    @Insert
    long insertGrade(Grade grade);

    @Update
    void updateGrade(Grade grade);

    @Delete
    void deleteGrade(Grade grade);
}
