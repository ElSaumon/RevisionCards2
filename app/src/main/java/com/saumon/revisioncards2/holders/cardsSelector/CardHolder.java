package com.saumon.revisioncards2.holders.cardsSelector;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.injection.Injection;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.utils.CardViewModel;
import com.saumon.revisioncards2.utils.CardsSelection;
import com.unnamed.b.atv.model.TreeNode;

import java.lang.ref.WeakReference;
import java.util.List;

public class CardHolder extends TreeNode.BaseNodeViewHolder<CardHolder.IconTreeItem> {
    private TreeNode node;
    IconTreeItem iconTreeItem;
    private View nodeView;
    private CardViewModel cardViewModel;
    private CheckBox checkBox;
    boolean mustSpreadToParent = true;

    public CardHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, @NonNull IconTreeItem iconTreeItem) {
        this.node = node;
        this.iconTreeItem = iconTreeItem;

        LayoutInflater inflater = LayoutInflater.from(context);
        nodeView = inflater.inflate(R.layout.node_cards_revision_selector_card, null);

        float scale = context.getResources().getDisplayMetrics().density;
        nodeView.setPaddingRelative(
                (int) (10 + 20 * iconTreeItem.level * scale + 0.5f),
                nodeView.getPaddingTop(),
                nodeView.getPaddingEnd(),
                nodeView.getPaddingBottom()
        );

        configureViewModel();
        String nameToDisplay = iconTreeItem.card.getName();
        if (null == nameToDisplay || nameToDisplay.isEmpty()) {
            nameToDisplay = iconTreeItem.card.getText1() + " / " + iconTreeItem.card.getText2();
        }

        TextView textView = nodeView.findViewById(R.id.node_cards_revision_selector_card_text);
        textView.setText(nameToDisplay);
        checkBox = nodeView.findViewById(R.id.node_cards_revision_selector_card_card_check);
        checkBox.setOnCheckedChangeListener(this::onCheckBoxCheckedChange);
        new ShowScoreAsyncTask(this).execute();

        return nodeView;
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(context);
        cardViewModel = ViewModelProviders.of((FragmentActivity) context, viewModelFactory).get(CardViewModel.class);
    }

    private void onCheckBoxCheckedChange(View buttonView, boolean isChecked) {
        iconTreeItem.isChecked = isChecked;
        if (isChecked) {
            CardsSelection.getInstance().cardList.add(iconTreeItem.card);
        } else {
            CardsSelection.getInstance().cardList.remove(iconTreeItem.card);
        }
        ((TextView) ((Activity) context).findViewById(R.id.activity_cards_revision_selector_nb_selected_cards_text)).setText(context.getString(R.string.Nb_selected_cards, CardsSelection.getInstance().cardList.size()));
        ((Activity) context).findViewById(R.id.activity_cards_revision_selector_review_btn).setEnabled(!CardsSelection.getInstance().cardList.isEmpty());
        if (mustSpreadToParent) {
            toggleParentCheckBox();
        }
        mustSpreadToParent = true;
    }

    void toggleCheckBox(boolean isChecked) {
        checkBox.setChecked(isChecked);
    }

    private void toggleParentCheckBox() {
        boolean areAllChildNodesChecked = true;
        TreeNode parentNode = node.getParent();
        if (FolderHolder.class != parentNode.getViewHolder().getClass()) {
            return;
        }

        List<TreeNode> childNodeList = parentNode.getChildren();
        for (int i = 0; i < childNodeList.size(); i++) {
            TreeNode childNode = childNodeList.get(i);
            if (FolderHolder.class == childNode.getViewHolder().getClass()) {
                FolderHolder childHolder = ((FolderHolder) childNode.getViewHolder());
                if (!childHolder.iconTreeItem.isChecked) {
                    areAllChildNodesChecked = false;
                    break;
                }
            } else {
                CardHolder childHolder = ((CardHolder) childNode.getViewHolder());
                if (!childHolder.iconTreeItem.isChecked) {
                    areAllChildNodesChecked = false;
                    break;
                }
            }
        }

        FolderHolder parentHolder = ((FolderHolder) parentNode.getViewHolder());
        if (areAllChildNodesChecked != parentHolder.iconTreeItem.isChecked) {
            parentHolder.mustSpreadToChildren = false;
            parentHolder.toggleCheckBox(areAllChildNodesChecked);
        }
    }

    private void showScore(int cardScore) {
        TextView textView = nodeView.findViewById(R.id.node_cards_revision_selector_card_score_text);
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
        boolean isChecked = false;

        public IconTreeItem(Card card, int level) {
            this.card = card;
            this.level = level;
        }
    }
}
