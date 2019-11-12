package com.saumon.revisioncards2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.holders.cardsSelector.CardHolder;
import com.saumon.revisioncards2.holders.cardsSelector.FolderHolder;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.saumon.revisioncards2.utils.CardsSelection;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

public class CardsRevisionSelectorActivity extends BaseActivity {
    private TreeNode root;
    private AndroidTreeView treeView;
    private List<Long> displayedActivityFolderIdList;
    private List<Long> displayedActivityCardIdList;
    private boolean getCardsBinded = false;

    @Override
    public int getLayoutContentViewID() {
        return R.layout.activity_cards_revision_selector;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.Revision_selection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CardsSelection.getInstance().cardList.clear();

        View rootView = findViewById(android.R.id.content);
        ViewGroup containerView = rootView.findViewById(R.id.activity_cards_revision_selector_cards_tree_view_container);

        initTreeView();
        containerView.addView(treeView.getView());

        getParentFolders();
    }

    @Override
    protected Toolbar getToolbar() {
        return findViewById(R.id.activity_cards_revision_selector_toolbar);
    }

    @OnClick(R.id.activity_cards_revision_selector_review_btn)
    public void onClickReviewButton() {
        if (CardsSelection.getInstance().cardList.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.Must_select_cards), Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, CardsRevisionActivity.class);
        startActivity(intent);
    }

    private void initTreeView() {
        root = TreeNode.root();
        treeView = new AndroidTreeView(this, root);
    }

    private void getParentFolders() {
        displayedActivityFolderIdList = new ArrayList<>();
        cardViewModel.getParentFolders().observe(this, this::updateActivityFolderList);
    }

    private void updateActivityFolderList(@NonNull List<Folder> folders) {
        for (Folder folder : folders) {
            if (!displayedActivityFolderIdList.contains(folder.getId())) {
                TreeNode folderNode = new TreeNode(new FolderHolder.IconTreeItem(folder, 0)).setViewHolder(new FolderHolder(this));
                treeView.addNode(root, folderNode);
                displayedActivityFolderIdList.add(folder.getId());
            }
        }
        if (!getCardsBinded) {
            getCardsWithoutParent();
            getCardsBinded = true;
        }
    }

    private void getCardsWithoutParent() {
        displayedActivityCardIdList = new ArrayList<>();
        cardViewModel.getCardsWithoutParent().observe(this, this::updateActivityCardList);
    }

    private void updateActivityCardList(@NonNull List<Card> cards) {
        for (Card card : cards) {
            if (!displayedActivityCardIdList.contains(card.getId())) {
                TreeNode cardNode = new TreeNode(new CardHolder.IconTreeItem(card, 0)).setViewHolder(new CardHolder(this));
                treeView.addNode(root, cardNode);
                displayedActivityCardIdList.add(card.getId());
            }
        }
    }
}
