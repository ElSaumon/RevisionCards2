package com.saumon.revisioncards2.utils;

import com.saumon.revisioncards2.models.Card;

import java.util.ArrayList;
import java.util.List;

public class CardsSelection {
    private static final CardsSelection INSTANCE = new CardsSelection();

    public List<Card> cardList = new ArrayList<>();

    private CardsSelection() {}

    public static CardsSelection getInstance() {
        return INSTANCE;
    }
}
