package com.saumon.revisioncards2.injections;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.saumon.revisioncards2.repositories.CardDataRepository;
import com.saumon.revisioncards2.repositories.FolderDataRepository;
import com.saumon.revisioncards2.repositories.GradeDataRepository;
import com.saumon.revisioncards2.utils.CardViewModel;

import java.util.concurrent.Executor;

@SuppressWarnings("unchecked cast")
public class ViewModelFactory implements ViewModelProvider.Factory {
    private final FolderDataRepository folderDataSource;
    private final CardDataRepository cardDataSource;
    private final GradeDataRepository gradeDataSource;
    private final Executor executor;

    public ViewModelFactory(FolderDataRepository folderDataSource, CardDataRepository cardDataSource, GradeDataRepository gradeDataSource, Executor executor) {
        this.folderDataSource = folderDataSource;
        this.cardDataSource = cardDataSource;
        this.gradeDataSource = gradeDataSource;
        this.executor = executor;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(CardViewModel.class)) {
            return (T) new CardViewModel(folderDataSource, cardDataSource, gradeDataSource, executor);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
