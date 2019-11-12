package com.saumon.revisioncards2.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.saumon.revisioncards2.database.RevisionCardsDatabase;
import com.saumon.revisioncards2.injection.Injection;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;

import java.util.Date;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;

class DatabaseUtils {
    public static void fillDatabase(@NonNull Context context) {
        emptyDatabase(context);

        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(context);
        CardViewModel cardViewModel = ViewModelProviders.of((FragmentActivity) context, viewModelFactory).get(CardViewModel.class);

        for (int if0 = 1; if0 < 6; if0++) {
            Folder folder0 = new Folder(new Date(), null, "Thème " + if0);
            cardViewModel.createFolderSync(folder0);
            for (int if1 = 1; if1 < 6; if1++) {
                Folder folder1 = new Folder(new Date(), folder0.getId(), "Partie " + if0 + "::" + if1);
                cardViewModel.createFolderSync(folder1);
                for (int if2 = 1; if2 < 6; if2++) {
                    Folder folder2 = new Folder(new Date(), folder1.getId(), "Sous-partie " + if0 + "::" + if1 + "::" + if2);
                    cardViewModel.createFolderSync(folder2);
                    for (int ic = 1; ic < 6; ic++) {
                        Card card = new Card(new Date(), folder2.getId(), "Fiche " + if0 + "::" + if1 + "::" + if2 + "::" + ic, "Texte " + if0 + "::" + if1 + "::" + if2 + "::" + ic + "::1", "Texte " + if0 + "::" + if1 + "::" + if2 + "::" + ic + "::2");
                        cardViewModel.createCardSync(card);
                    }
                }
                for (int ic = 1; ic < 6; ic++) {
                    Card card = new Card(new Date(), folder1.getId(), "Fiche " + if0 + "::" + if1 + "::" + ic, "Texte " + if0 + "::" + if1 + "::" + ic + "::1", "Texte " + if0 + "::" + if1 + "::" + ic + "::2");
                    cardViewModel.createCardSync(card);
                }
            }
            for (int ic = 1; ic < 6; ic++) {
                Card card = new Card(new Date(), folder0.getId(), "Fiche " + if0 + "::" + ic, "Texte " + if0 + "::" + ic + "::1", "Texte " + if0 + "::" + ic + "::2");
                cardViewModel.createCardSync(card);
            }
        }
        for (int ic = 1; ic < 6; ic++) {
            Card card = new Card(new Date(), null, "Fiche " + ic, "Texte " + ic + "::1", "Texte " + ic + "::2");
            cardViewModel.createCardSync(card);
        }
    }

    public static void emptyDatabase(@NonNull Context context) {
        SQLiteDatabase database = SQLiteDatabase.openDatabase(context.getDatabasePath(RevisionCardsDatabase.DATABASE_NAME).getPath(), null, OPEN_READWRITE);
        String[] tableList = {"Folder", "Card", "Grade"};
        for (String table :tableList) {
            try {
                database.execSQL("DELETE FROM " + table);
            } catch (SQLiteException ignored) {}
            try {
                database.execSQL("DELETE FROM sqlite_sequence WHERE name='" + table + "'");
            } catch (SQLiteException ignored) {}
        }
        database.close();
    }
}
