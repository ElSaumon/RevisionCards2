package com.saumon.revisioncards2.holders.cardsSelector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.github.johnkil.print.PrintView;
import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.injection.Injection;
import com.saumon.revisioncards2.injections.ViewModelFactory;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.saumon.revisioncards2.utils.CardViewModel;
import com.unnamed.b.atv.model.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class FolderHolder extends TreeNode.BaseNodeViewHolder<FolderHolder.IconTreeItem> {
    private TreeNode node;
    IconTreeItem iconTreeItem;
    private CardViewModel cardViewModel;
    private PrintView arrowIconView;
    private CheckBox checkBox;
    private List<Long> displayedChildFolderIdList;
    private List<Long> displayedChildCardIdList;
    private boolean getFoldersBinded = false;
    private boolean getCardsBinded = false;
    boolean mustSpreadToChildren = true;
    private boolean mustSpreadToParent = true;

    public FolderHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, @NonNull IconTreeItem iconTreeItem) {
        this.node = node;
        this.iconTreeItem = iconTreeItem;

        LayoutInflater inflater = LayoutInflater.from(context);
        View nodeView = inflater.inflate(R.layout.node_cards_revision_selector_folder, null);

        float scale = context.getResources().getDisplayMetrics().density;
        nodeView.setPaddingRelative(
                (int) (10 + 20 * iconTreeItem.level * scale + 0.5f),
                nodeView.getPaddingTop(),
                nodeView.getPaddingEnd(),
                nodeView.getPaddingBottom()
        );

        configureViewModel();
        displayedChildFolderIdList = new ArrayList<>();
        displayedChildCardIdList = new ArrayList<>();

        TextView textView = nodeView.findViewById(R.id.node_cards_revision_selector_folder_text);
        textView.setText(iconTreeItem.folder.getName());
        arrowIconView = nodeView.findViewById(R.id.node_cards_revision_selector_folder_arrow_icon);
        checkBox = nodeView.findViewById(R.id.node_cards_revision_selector_card_folder_check);
        checkBox.setOnCheckedChangeListener(this::onCheckBoxCheckedChange);

        return nodeView;
    }

    @Override
    public void toggle(boolean active) {
        arrowIconView.setIconText(context.getResources().getString(active ? R.string.ic_keyboard_arrow_down : R.string.ic_keyboard_arrow_right));
        if (active && !getFoldersBinded) {
            cardViewModel.getFoldersByParentId(iconTreeItem.folder.getId()).observe((LifecycleOwner) context, this::updateChildFolderList);
            getFoldersBinded = true;
        }
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(context);
        cardViewModel = ViewModelProviders.of((FragmentActivity) context, viewModelFactory).get(CardViewModel.class);
    }

    private void onCheckBoxCheckedChange(View buttonView, boolean isChecked) {
        iconTreeItem.isChecked = isChecked;
        if (isChecked) {
            if (!node.isExpanded()) {
                node.getViewHolder().getTreeView().expandNode(node);
            }
        }
        if (mustSpreadToChildren) {
            toggleChildrenCheckBoxes(isChecked);
        }
        if (mustSpreadToParent) {
            toggleParentCheckBox();
        }
        mustSpreadToChildren = true;
        mustSpreadToParent = true;
    }

    void toggleCheckBox(boolean isChecked) {
        checkBox.setChecked(isChecked);
    }

    private void toggleChildrenCheckBoxes(boolean isChecked) {
        List<TreeNode> childNodeList = node.getChildren();
        for (int i = 0; i < childNodeList.size(); i++) {
            TreeNode childNode = childNodeList.get(i);
            if (FolderHolder.class == childNode.getViewHolder().getClass()) {
                FolderHolder childHolder = ((FolderHolder) childNode.getViewHolder());
                if (isChecked != childHolder.iconTreeItem.isChecked) {
                    childHolder.mustSpreadToParent = false;
                    childHolder.toggleCheckBox(isChecked);
                }
                if (!childNode.isExpanded()) {
                    childNode.getViewHolder().getTreeView().expandNode(childNode);
                }
            } else {
                CardHolder childHolder = (CardHolder) childNode.getViewHolder();
                if (isChecked != childHolder.iconTreeItem.isChecked) {
                    childHolder.mustSpreadToParent = false;
                    childHolder.toggleCheckBox(isChecked);
                }
            }
        }
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

    private void updateChildFolderList(@NonNull List<Folder> folders) {
        for (Folder folder : folders) {
            if (!displayedChildFolderIdList.contains(folder.getId())) {
                TreeNode folderNode = new TreeNode(new FolderHolder.IconTreeItem(folder, iconTreeItem.level + 1)).setViewHolder(new FolderHolder(context));
                getTreeView().addNode(node, folderNode);
                displayedChildFolderIdList.add(folder.getId());
                if (iconTreeItem.isChecked) {
                    FolderHolder folderHolder = (FolderHolder) folderNode.getViewHolder();
                    folderHolder.mustSpreadToParent = false;
                    folderHolder.toggleCheckBox(true);
                }
            }
        }
        if (!getCardsBinded) {
            cardViewModel.getCardsByFolderId(iconTreeItem.folder.getId()).observe((LifecycleOwner) context, this::updateChildCardList);
            getCardsBinded = true;
        }
    }

    private void updateChildCardList(@NonNull List<Card> cards) {
        for (Card card : cards) {
            if (!displayedChildCardIdList.contains(card.getId())) {
                TreeNode cardNode = new TreeNode(new CardHolder.IconTreeItem(card, iconTreeItem.level + 1)).setViewHolder(new CardHolder(context));
                getTreeView().addNode(node, cardNode);
                displayedChildCardIdList.add(card.getId());
                if (iconTreeItem.isChecked) {
                    CardHolder cardHolder = (CardHolder) cardNode.getViewHolder();
                    cardHolder.mustSpreadToParent = false;
                    cardHolder.toggleCheckBox(true);
                }
            }
        }
    }

    public static class IconTreeItem {
        Folder folder;
        int level;
        boolean isChecked = false;

        public IconTreeItem(Folder folder, int level) {
            this.folder = folder;
            this.level = level;
        }
    }
}
