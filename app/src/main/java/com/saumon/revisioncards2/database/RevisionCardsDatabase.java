package com.saumon.revisioncards2.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.saumon.revisioncards2.database.dao.CardDao;
import com.saumon.revisioncards2.database.dao.FolderDao;
import com.saumon.revisioncards2.database.dao.GradeDao;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.saumon.revisioncards2.models.Grade;
import com.saumon.revisioncards2.utils.DateTypeConverter;

@Database(entities = {Folder.class, Card.class, Grade.class}, version = 1, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class RevisionCardsDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "RevisionCardsDatabase2.db";
    private static volatile RevisionCardsDatabase INSTANCE;

    public abstract FolderDao folderDao();
    public abstract CardDao cardDao();
    public abstract GradeDao gradeDao();

    public static RevisionCardsDatabase getInstance(Context context) {
        if (null == INSTANCE) {
            synchronized (RevisionCardsDatabase.class) {
                if (null == INSTANCE) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            RevisionCardsDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
