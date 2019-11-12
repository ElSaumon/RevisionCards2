package com.saumon.revisioncards2.injection;

import android.content.Context;

import androidx.annotation.NonNull;

import com.saumon.revisioncards2.database.RevisionCardsDatabase;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.repositories.CardDataRepository;
import com.saumon.revisioncards2.repositories.FolderDataRepository;
import com.saumon.revisioncards2.repositories.GradeDataRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Injection {
    @NonNull
    private static FolderDataRepository provideFolderDataSource(Context context) {
        RevisionCardsDatabase database = RevisionCardsDatabase.getInstance(context);
        return new FolderDataRepository(database.folderDao());
    }

    @NonNull
    private static CardDataRepository provideCardDataSource(Context context) {
        RevisionCardsDatabase database = RevisionCardsDatabase.getInstance(context);
        return new CardDataRepository(database.cardDao());
    }

    @NonNull
    private static GradeDataRepository provideGradeDataSource(Context context) {
        RevisionCardsDatabase database = RevisionCardsDatabase.getInstance(context);
        return new GradeDataRepository(database.gradeDao());
    }

    @NonNull
    private static Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @NonNull
    public static ViewModelFactory provideViewModelFactory(Context context) {
        FolderDataRepository dataSourceFolder = provideFolderDataSource(context);
        CardDataRepository dataSourceCard = provideCardDataSource(context);
        GradeDataRepository dataSourceGrade = provideGradeDataSource(context);
        Executor executor = provideExecutor();
        return new ViewModelFactory(dataSourceFolder, dataSourceCard, dataSourceGrade, executor);
    }
}
