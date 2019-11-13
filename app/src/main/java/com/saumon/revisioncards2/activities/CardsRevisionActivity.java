package com.saumon.revisioncards2.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Grade;
import com.saumon.revisioncards2.utils.CardsSelection;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class CardsRevisionActivity extends BaseActivity {
    @BindView(R.id.activity_cards_revision_ok_btn) Button okBtn;
    @BindView(R.id.activity_cards_revision_middle_btn) Button middleBtn;
    @BindView(R.id.activity_cards_revision_ko_btn) Button koBtn;

    private List<Card> shuffledCardList;
    private int nextCardIndex = 0;
    private Card card;

    @Override
    public int getLayoutContentViewID() {
        return R.layout.activity_cards_revision;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.Revision);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNextCard();
    }

    @Override
    protected Toolbar getToolbar() {
        return findViewById(R.id.activity_cards_revision_toolbar);
    }

    @OnClick(R.id.activity_cards_revision_text2_text)
    public void onClickText2Text(@NonNull TextView textView) {
        textView.setText(card.getTextToHide());
        textView.setBackgroundResource(R.drawable.borders);
        enableButtons();
    }

    @OnClick(R.id.activity_cards_revision_ok_btn)
    public void onClickOkButton() {
        onClickGradeButton(2);
    }

    @OnClick(R.id.activity_cards_revision_middle_btn)
    public void onClickMiddleButton() {
        onClickGradeButton(1);
    }

    @OnClick(R.id.activity_cards_revision_ko_btn)
    public void onClickKoButton() {
        onClickGradeButton(0);
    }

    private void showNextCard() {
        if (null == shuffledCardList) {
            shuffledCardList = CardsSelection.getInstance().cardList;
            Collections.shuffle(shuffledCardList);
        }
        if (nextCardIndex == shuffledCardList.size()) {
            Collections.shuffle(shuffledCardList);
            nextCardIndex = 0;
        }
        card = shuffledCardList.get(nextCardIndex);
        if (null == card.getName() || card.getName().isEmpty()) {
            ((TextView) findViewById(R.id.activity_cards_revision_current_card_text)).setText("");
        } else {
            ((TextView) findViewById(R.id.activity_cards_revision_current_card_text)).setText(card.getName());
        }
        ((TextView) findViewById(R.id.activity_cards_revision_text1_text)).setText(card.getTextToShow());
        findViewById(R.id.activity_cards_revision_text2_text).setBackgroundResource(R.drawable.borders_fill_black);
        disableButtons();
        new ShowScoreAsyncTask(this).execute();
        nextCardIndex++;
    }

    private void onClickGradeButton(int gradeValue) {
        new AddGradeToCardAsyncTask(this, gradeValue).execute();
    }

    private void enableButtons() {
        okBtn.setEnabled(true);
        middleBtn.setEnabled(true);
        koBtn.setEnabled(true);
        okBtn.setBackgroundResource(R.drawable.button_ok);
        middleBtn.setBackgroundResource(R.drawable.button_middle);
        koBtn.setBackgroundResource(R.drawable.button_ko);
        okBtn.setTextColor(getResources().getColor(android.R.color.black));
        middleBtn.setTextColor(getResources().getColor(android.R.color.black));
        koBtn.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void disableButtons() {
        okBtn.setEnabled(false);
        middleBtn.setEnabled(false);
        koBtn.setEnabled(false);
        okBtn.setBackgroundResource(R.drawable.button_disable);
        middleBtn.setBackgroundResource(R.drawable.button_disable);
        koBtn.setBackgroundResource(R.drawable.button_disable);
        okBtn.setTextColor(getResources().getColor(android.R.color.darker_gray));
        middleBtn.setTextColor(getResources().getColor(android.R.color.darker_gray));
        koBtn.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void showScore(int cardScore) {
        TextView textView = findViewById(R.id.activity_cards_revision_score_text);
        if (-1 == cardScore) {
            textView.setText(getString(R.string.No_score));
            textView.setBackgroundResource(android.R.color.transparent);
            return;
        }
        textView.setText(getString(R.string.Score_display, cardScore));
        int color;
        if (cardScore < 33) {
            color = R.color.red;
        } else if (cardScore < 66) {
            color = R.color.orange;
        } else {
            color = R.color.green;
        }
        textView.setBackgroundResource(color);
    }

    private Grade addGradeToCard(int gradeValue) {
        return cardViewModel.addGradeToCard(card, gradeValue);
    }

    private static class ShowScoreAsyncTask extends AsyncTask<Void, Void, Integer> {
        private final WeakReference<CardsRevisionActivity> activity;

        ShowScoreAsyncTask(CardsRevisionActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return activity.get().cardViewModel.getCardScore(activity.get().card.getId());
        }

        @Override
        protected void onPostExecute(Integer cardScore) {
            super.onPostExecute(cardScore);
            activity.get().showScore(cardScore);
        }
    }

    private static class AddGradeToCardAsyncTask extends AsyncTask<Void, Void, Grade> {
        private final WeakReference<CardsRevisionActivity> activity;
        private int gradeValue;

        AddGradeToCardAsyncTask(CardsRevisionActivity activity, int gradeValue) {
            this.activity = new WeakReference<>(activity);
            this.gradeValue = gradeValue;
        }

        @Override
        protected Grade doInBackground(Void... params) {
            return activity.get().addGradeToCard(gradeValue);
        }

        @Override
        protected void onPostExecute(Grade grade) {
            super.onPostExecute(grade);
            Handler handler = new Handler();
            do {
                handler.postDelayed(() -> {}, 200);
            } while (0 == grade.getId());
            activity.get().cardViewModel.reverseSideToShow(activity.get().card);
            activity.get().showNextCard();
        }
    }
}
