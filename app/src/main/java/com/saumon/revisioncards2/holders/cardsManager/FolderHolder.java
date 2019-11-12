package com.saumon.revisioncards2.holders.cardsManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.github.johnkil.print.PrintView;
import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.activities.CardsManagerActivity;
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
    private IconTreeItem iconTreeItem;
    private View nodeView;
    private CardViewModel cardViewModel;
    private TextView textView;
    private PrintView arrowIconView;
    private List<Long> displayedChildFolderIdList;
    private List<Long> displayedChildCardIdList;
    private boolean getFoldersBinded = false;
    private boolean getCardsBinded = false;

    public FolderHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, @NonNull IconTreeItem iconTreeItem) {
        this.node = node;
        this.iconTreeItem = iconTreeItem;

        LayoutInflater inflater = LayoutInflater.from(context);
        nodeView = inflater.inflate(R.layout.node_cards_manager_folder, null);

        float scale = context.getResources().getDisplayMetrics().density;
        nodeView.setPaddingRelative(
                (int) (10 + 20 * iconTreeItem.level * scale + 0.5f),
                nodeView.getPaddingTop(),
                nodeView.getPaddingEnd(),
                nodeView.getPaddingBottom()
        );

        configureButtonsOnClick();
        configureViewModel();
        displayedChildFolderIdList = new ArrayList<>();
        displayedChildCardIdList = new ArrayList<>();

        textView = nodeView.findViewById(R.id.node_cards_manager_folder_text);
        textView.setText(iconTreeItem.folder.getName());
        arrowIconView = nodeView.findViewById(R.id.node_cards_manager_folder_arrow_icon);

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

    private void configureButtonsOnClick() {
        nodeView.findViewById(R.id.node_cards_manager_folder_edit_icon).setOnClickListener(v -> editGetName());
        nodeView.findViewById(R.id.node_cards_manager_folder_delete_icon).setOnClickListener(v -> deleteAskConfirmation());
    }

    private void configureViewModel() {
        ViewModelFactory viewModelFactory = Injection.provideViewModelFactory(context);
        cardViewModel = ViewModelProviders.of((FragmentActivity) context, viewModelFactory).get(CardViewModel.class);
    }

    private void updateChildFolderList(@NonNull List<Folder> folders) {
        if (!((CardsManagerActivity) context).isOnCreate()) {
            for (Folder folder : folders) {
                if (!displayedChildFolderIdList.contains(folder.getId())) {
                    TreeNode folderNode = new TreeNode(new FolderHolder.IconTreeItem(folder, iconTreeItem.level + 1)).setViewHolder(new FolderHolder(context));
                    getTreeView().addNode(node, folderNode);
                    displayedChildFolderIdList.add(folder.getId());
                }
            }
        }
        if (!getCardsBinded) {
            cardViewModel.getCardsByFolderId(iconTreeItem.folder.getId()).observe((LifecycleOwner) context, this::updateChildCardList);
            getCardsBinded = true;
        }
    }

    private void updateChildCardList(@NonNull List<Card> cards) {
        if (((CardsManagerActivity) context).isOnCreate()) {
            return;
        }
        for (Card card : cards) {
            if (!displayedChildCardIdList.contains(card.getId())) {
                TreeNode cardNode = new TreeNode(new CardHolder.IconTreeItem(card, iconTreeItem.level + 1)).setViewHolder(new CardHolder(context));
                getTreeView().addNode(node, cardNode);
                displayedChildCardIdList.add(card.getId());
            }
        }
    }

    private void editGetName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_folder, null);

        builder.setView(dialogView);
        switch (iconTreeItem.level) {
            case 0:
                builder.setTitle(context.getString(R.string.Edit_folder_0));
                break;
            case 1:
                builder.setTitle(context.getString(R.string.Edit_folder_1));
                break;
            case 2:
                builder.setTitle(context.getString(R.string.Edit_folder_2));
                break;
        }
        builder.setNegativeButton(context.getString(R.string.Cancel), null)
                .setPositiveButton(context.getString(R.string.Edit), this::edit);
        AlertDialog dialog = builder.create();

        EditText nameText = dialogView.findViewById(R.id.dialog_edit_folder_name_text);
        nameText.setText(iconTreeItem.folder.getName());
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
        String name = ((EditText) ((AlertDialog) dialog).findViewById(R.id.dialog_edit_folder_name_text)).getText().toString();
        if (name.isEmpty()) {
            return;
        }
        iconTreeItem.folder.setName(name);
        cardViewModel.updateFolder(iconTreeItem.folder);
        textView.setText(name);
    }

    private void deleteAskConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        switch (iconTreeItem.level) {
            case 0:
                builder.setTitle(context.getString(R.string.Delete_folder_0))
                        .setMessage(context.getString(R.string.Delete_folder_0_confirmation, iconTreeItem.folder.getName()));
                break;
            case 1:
                builder.setTitle(context.getString(R.string.Delete_folder_1))
                        .setMessage(context.getString(R.string.Delete_folder_1_confirmation, iconTreeItem.folder.getName()));
                break;
            case 2:
                builder.setTitle(context.getString(R.string.Delete_folder_2))
                        .setMessage(context.getString(R.string.Delete_folder_2_confirmation, iconTreeItem.folder.getName()));
                break;
        }
        builder.setNegativeButton(context.getString(R.string.Cancel), null)
                .setPositiveButton(context.getString(R.string.Delete), this::delete)
                .create()
                .show();
    }

    private void delete(DialogInterface dialog, int which) {
        cardViewModel.deleteFolder(iconTreeItem.folder);
        getTreeView().removeNode(node);
    }

    public static class IconTreeItem {
        Folder folder;
        int level;

        public IconTreeItem(Folder folder, int level) {
            this.folder = folder;
            this.level = level;
        }
    }
}
