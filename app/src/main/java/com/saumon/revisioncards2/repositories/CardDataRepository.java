package com.saumon.revisioncards2.repositories;

import androidx.lifecycle.LiveData;

import com.saumon.revisioncards2.database.dao.CardDao;
import com.saumon.revisioncards2.models.Card;

import java.util.List;

public class CardDataRepository {
    private final CardDao cardDao;

    public CardDataRepository(CardDao cardDao) {
        this.cardDao = cardDao;
    }

    public LiveData<List<Card>> getCards() {
        return cardDao.getCards();
    }

    public LiveData<List<Card>> getCardsWithoutParent() {
        return cardDao.getCardsWithoutParent();
    }

    public List<Card> getCardsWithoutParentSync() {
        return cardDao.getCardsWithoutParentSync();
    }

    public LiveData<List<Card>> getCardsByFolderId(long folderId) {
        return cardDao.getCardsByFolderId(folderId);
    }

    public List<Card> getCardsByFolderIdSync(long folderId) {
        return cardDao.getCardsByFolderIdSync(folderId);
    }

    public void createCard(Card card) {
        long cardId = cardDao.insertCard(card);
        card.setId(cardId);
    }

    public void updateCard(Card card) {
        cardDao.updateCard(card);
    }

    public void deleteCard(Card card) {
        cardDao.deleteCard(card);
    }
}
