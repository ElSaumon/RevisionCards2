package com.saumon.revisioncards2.holders.cardsManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.injection.Injection;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.utils.CardViewModel;
import com.unnamed.b.atv.model.TreeNode;

import java.lang.ref.WeakReference;

public class CardHolder extends TreeNode.BaseNodeViewHolder<CardHolder.IconTreeItem> {
    private TreeNode node;
    private IconTreeItem iconTreeItem;
    private View nodeView;
    private CardViewModel cardViewModel;
    private TextView textView;

    public CardHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, @NonNull IconTreeItem iconTreeItem) {
        this.node = node;
        this.iconTreeItem = iconTreeItem;

        LayoutInflater inflater = LayoutInflater.from(context);
        nodeView = inflater.inflate(R.layout.node_cards_manager_card, null);

        float scale = context.getResources().getDisplayMetrics().density;
        nodeView.setPaddingRelative(
                (int) (10 + 20 * iconTreeItem.level * scale + 0.5f),
                nodeView.getPaddingTop(),
                nodeView.getPaddingEnd(),
                nodeView.getPaddingBottom()
        );

        configureButtonsOnClick();
        configureViewModel();

        String nameToDisplay = iconTreeItem.card.getName();
        if (null == nameToDisplay || nameToDisplay.isEmpty()) {
            nameToDisplay = iconTreeItem.card.getText1() + " / " + iconTreeItem.card.getText2();
        }

        textView = nodeView.findViewById(R.id.node_cards_manager_card_text);
        textView.setText(nameToDisplay);
        new ShowScoreAsyncTask(this).execute();

        return nodeView;
    }

    private void configureButtonsOnClick() {
        nodeView.findViewById(R.id.node_cards_manager_card_edit_icon).setOnClickListener(v -> editGetTexts());
        nodeView.findViewById(R.id.node_cards_manager_card_delete_icon).setOnClickListener(v -> deleteAskConfirmation());
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(context);
        cardViewModel = ViewModelProviders.of((FragmentActivity) context, viewModelFactory).get(CardViewModel.class);
    }

    private void editGetTexts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_card, null);

        builder.setView(dialogView)
                .setTitle(context.getString(R.string.Edit_card))
                .setNegativeButton(context.getString(R.string.Cancel), null)
                .setPositiveButton(context.getString(R.string.Edit), this::edit);
        AlertDialog dialog = builder.create();

        ((EditText) dialogView.findViewById(R.id.dialog_edit_card_text1_text)).setText(iconTreeItem.card.getText1());
        ((EditText) dialogView.findViewById(R.id.dialog_edit_card_text2_text)).setText(iconTreeItem.card.getText2());
        EditText nameText = dialogView.findViewById(R.id.dialog_edit_card_name_text);
        nameText.setText(iconTreeItem.card.getName());
        nameText.setSelection(nameText.getText().length());
        nameText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Window window = dialog.getWindow();
                if (null == window) {
                    return;
                }
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        dialog.show();
    }

    private void edit(DialogInterface dialog, int which) {
        String name = ((EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_edit_card_name_text)).getText().toString();
        String text1 = ((EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_edit_card_text1_text)).getText().toString();
        String text2 = ((EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_edit_card_text2_text)).getText().toString();
        if (text1.isEmpty() || text2.isEmpty()) {
            return;
        }
        String nameToDisplay;
        if (!name.isEmpty()) {
            nameToDisplay = name;
        } else {
            nameToDisplay = text1 + " / " + text2;
        }
        iconTreeItem.card.setName(name);
        iconTreeItem.card.setText1(text1);
        iconTreeItem.card.setText2(text2);
        cardViewModel.updateCard(iconTreeItem.card);
        textView.setText(nameToDisplay);
    }

    private void deleteAskConfirmation() {
        String nameToDisplay;
        if (null != iconTreeItem.card.getName() && !iconTreeItem.card.getName().isEmpty()) {
            nameToDisplay = iconTreeItem.card.getName();
        } else {
            nameToDisplay = iconTreeItem.card.getText1() + " / " + iconTreeItem.card.getText2();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.Delete_card))
                .setMessage(context.getString(R.string.Delete_card_confirmation, nameToDisplay))
                .setNegativeButton(context.getString(R.string.Cancel), null)
                .setPositiveButton(context.getString(R.string.Delete), this::delete)
                .create()
                .show();
    }

    private void delete(DialogInterface dialog, int which) {
        cardViewModel.deleteCard(iconTreeItem.card);
        getTreeView().removeNode(node);
    }

    private void showScore(int cardScore) {
        TextView textView = nodeView.findViewById(R.id.node_cards_manager_card_score_text);
        if (-1 == cardScore) {
            textView.setText("");
            textView.setBackgroundResource(android.R.color.transparent);
            return;
        }
        textView.setText(String.valueOf(cardScore));
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

    private static class ShowScoreAsyncTask extends AsyncTask<Void, Void, Integer> {
        private final WeakReference<CardHolder> holder;

        ShowScoreAsyncTask(CardHolder holder) {
            this.holder = new WeakReference<>(holder);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return holder.get().cardViewModel.getCardScore(holder.get().iconTreeItem.card.getId());
        }

        @Override
        protected void onPostExecute(Integer cardScore) {
            super.onPostExecute(cardScore);
            holder.get().showScore(cardScore);
        }
    }

    public static class IconTreeItem {
        Card card;
        int level;

        public IconTreeItem(Card card, int level) {
            this.card = card;
            this.level = level;
        }
    }
}
