package com.saumon.revisioncards2.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.saumon.revisioncards2.models.Grade;
import com.saumon.revisioncards2.repositories.CardDataRepository;
import com.saumon.revisioncards2.repositories.FolderDataRepository;
import com.saumon.revisioncards2.repositories.GradeDataRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class CardViewModel extends ViewModel {
    private final FolderDataRepository folderDataSource;
    private final CardDataRepository cardDataSource;
    private final GradeDataRepository gradeDataSource;
    private final Executor executor;

    public CardViewModel(FolderDataRepository folderDataSource, CardDataRepository cardDataSource, GradeDataRepository gradeDataSource, Executor executor) {
        this.folderDataSource = folderDataSource;
        this.cardDataSource = cardDataSource;
        this.gradeDataSource = gradeDataSource;
        this.executor = executor;
    }

    public LiveData<List<Folder>> getFolders() {
        return folderDataSource.getFolders();
    }

    public LiveData<List<Folder>> getParentFolders() {
        return folderDataSource.getParentFolders();
    }

    public List<Folder> getParentFoldersSync() {
        return folderDataSource.getParentFoldersSync();
    }

    public LiveData<List<Folder>> getFoldersByParentId(long parentId) {
        return folderDataSource.getFoldersByParentId(parentId);
    }

    public List<Folder> getFoldersByParentIdSync(long parentId) {
        return folderDataSource.getFoldersByParentIdSync(parentId);
    }

    public void createFolder(Folder folder) {
        executor.execute(() -> folderDataSource.createFolder(folder));
    }

    public void createFolderSync(Folder folder) {
        folderDataSource.createFolder(folder);
    }

    public void updateFolder(Folder folder) {
        executor.execute(() -> folderDataSource.updateFolder(folder));
    }

    public void deleteFolder(Folder folder) {
        executor.execute(() -> folderDataSource.deleteFolder(folder));
    }

    public LiveData<List<Card>> getCards() {
        return cardDataSource.getCards();
    }

    public LiveData<List<Card>> getCardsWithoutParent() {
        return cardDataSource.getCardsWithoutParent();
    }

    public List<Card> getCardsWithoutParentSync() {
        return cardDataSource.getCardsWithoutParentSync();
    }

    public LiveData<List<Card>> getCardsByFolderId(long folderId) {
        return cardDataSource.getCardsByFolderId(folderId);
    }

    public List<Card> getCardsByFolderIdSync(long folderId) {
        return cardDataSource.getCardsByFolderIdSync(folderId);
    }

    public void createCard(Card card) {
        executor.execute(() -> cardDataSource.createCard(card));
    }

    public void createCardSync(Card card) {
        cardDataSource.createCard(card);
    }

    public void updateCard(Card card) {
        executor.execute(() -> cardDataSource.updateCard(card));
    }

    public void deleteCard(Card card) {
        executor.execute(() -> cardDataSource.deleteCard(card));
    }

    public LiveData<List<Grade>> getGrades() {
        return gradeDataSource.getGrades();
    }

    public List<Grade> getGradesFromCardSync(long cardId) {
        return gradeDataSource.getGradesFromCard(cardId);
    }

    public void createGrade(Grade grade) {
        executor.execute(() -> gradeDataSource.createGrade(grade));
    }

    public void createGradeSync(Grade grade) {
        gradeDataSource.createGrade(grade);
    }

    public void updateGrade(Grade grade) {
        executor.execute(() -> gradeDataSource.updateGrade(grade));
    }

    public void deleteGrade(Grade grade) {
        executor.execute(() -> gradeDataSource.deleteGrade(grade));
    }

    public int getCardScore(long cardId) {
        List<Grade> gradeList = getGradesFromCardSync(cardId);
        if (gradeList.isEmpty()) {
            return -1;
        }
        int score = 0;
        for (int i = 0; i < gradeList.size(); i++) {
            score += gradeList.get(i).getValue();
        }
        score *= 100;
        score /= 2;
        score /= gradeList.size();
        return score;
    }

    public void reverseSideToShow(@NonNull Card card) {
        card.reverseSideToShow();
        updateCard(card);
    }

    public Grade addGradeToCard(@NonNull Card card, int gradeValue) {
        List<Grade> gradeList = getGradesFromCardSync(card.getId());
        if (10 == gradeList.size()) {
            deleteGrade(gradeList.get(0));
        }
        Grade grade = new Grade(new Date(), card.getId(), gradeValue);
        createGrade(grade);
        return grade;
    }
}
