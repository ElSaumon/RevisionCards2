package com.saumon.revisioncards2.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.saumon.revisioncards2.R;
import com.saumon.revisioncards2.holders.cardsManager.CardHolder;
import com.saumon.revisioncards2.holders.cardsManager.FolderHolder;
import com.saumon.revisioncards2.models.Card;
import com.saumon.revisioncards2.models.Folder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.OnClick;

public class CardsManagerActivity extends BaseActivity {
    private static final String MODE_NONE = "none";
    private static final String MODE_SELECTION = "selection";
    private static final String MODE_CREATION = "creation";

    private ViewGroup containerView;
    private TreeNode root;
    private AndroidTreeView treeView;
    private List<Long> displayedActivityFolderIdList;
    private List<Long> displayedActivityCardIdList;
    private boolean getCardsBinded = false;
    private View dialogView;
    public Long folder0Id, folder1Id, folder2Id;
    private boolean onCreate = false;
    private boolean addedFolderCard = false;
    private List<FolderMode> folder0Modes, folder1Modes, folder2Modes;

    @Override
    public int getLayoutContentViewID() {
        return R.layout.activity_cards_manager;
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.Cards);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = findViewById(android.R.id.content);
        containerView = rootView.findViewById(R.id.activity_cards_manager_tree_view_container);

        resetTreeView();
    }

    @Override
    protected Toolbar getToolbar() {
        return findViewById(R.id.activity_cards_manager_toolbar);
    }

    public void changeFolderId(int folder, Long value) throws IllegalArgumentException {
        switch (folder) {
            case 0:
                folder0Id = value;
                break;
            case 1:
                folder1Id = value;
                break;
            case 2:
                folder2Id = value;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }
    }

    public boolean isOnCreate() {
        return onCreate;
    }

    // region DisplayFoldersCards
    private void resetTreeView() {
        containerView.removeAllViews();
        displayedActivityFolderIdList = new ArrayList<>();
        displayedActivityCardIdList = new ArrayList<>();
        root = TreeNode.root();
        treeView = new AndroidTreeView(this, root);
        containerView.addView(treeView.getView());
        cardViewModel.getParentFolders().observe(this, this::updateActivityFolderList);
    }

    private void updateActivityFolderList(@NonNull List<Folder> folders) {
        if (!onCreate) {
            for (Folder folder : folders) {
                if (!displayedActivityFolderIdList.contains(folder.getId())) {
                    TreeNode folderNode = new TreeNode(new FolderHolder.IconTreeItem(folder, 0)).setViewHolder(new FolderHolder(this));
                    treeView.addNode(root, folderNode);
                    displayedActivityFolderIdList.add(folder.getId());
                }
            }
        }
        if (!getCardsBinded) {
            cardViewModel.getCardsWithoutParent().observe(this, this::updateActivityCardList);
            getCardsBinded = true;
        }
    }

    private void updateActivityCardList(@NonNull List<Card> cards) {
        if (onCreate) {
            return;
        }
        for (Card card : cards) {
            if (!displayedActivityCardIdList.contains(card.getId())) {
                TreeNode cardNode = new TreeNode(new CardHolder.IconTreeItem(card, 0)).setViewHolder(new CardHolder(this));
                treeView.addNode(root, cardNode);
                displayedActivityCardIdList.add(card.getId());
            }
        }
    }
    // endregion

    // region DialogAddFolderCard
    private class FolderMode {
        String key;
        String value;

        FolderMode(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }

    @OnClick(R.id.activity_cards_manager_add_btn)
    public void showDialogAddFolderCard() {
        Activity self = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = getLayoutInflater().inflate(R.layout.dialog_add_folder_card, null);

        AlertDialog dialog = builder
                .setView(dialogView)
                .setTitle(getString(R.string.Add_folder_card))
                .setNegativeButton(getString(R.string.Close), (dialog1, which) -> {
                    if (addedFolderCard) {
                        getCardsBinded = false;
                        resetTreeView();
                        addedFolderCard = false;
                    }
                    onCreate = false;
                })
                .setPositiveButton(getString(R.string.Add), null)
                .create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> addFolderCard(dialog, self)));

        try {
            initFoldersModes();
            getFoldersByParentId(0, null);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
            return;
        }

        bindEventsOnDialogElements(dialog);

        dialog.show();

        onCreate = true;
    }

    private void addFolderCard(AlertDialog dialog, Activity activity) {
        try {
            Folder folder0, folder1, folder2;
            folder0 = folder1 = folder2 = null;

            if (isFolderMode(0, MODE_SELECTION)) {
                Spinner spinnerFolder0 = dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_spinner);
                folder0 = (Folder) spinnerFolder0.getSelectedItem();
            } else if (isFolderMode(0, MODE_CREATION)) {
                String folder0Name = getFolderNameTextContent(0);
                if (!folder0Name.isEmpty()) {
                    folder0 = new Folder(new Date(), null, folder0Name);
                    cardViewModel.createFolder(folder0);
                    addedFolderCard = true;
                    while (0 == folder0.getId()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    folder0Id = folder0.getId();
                }
            }

            if (null != folder0) {
                if (isFolderMode(1, MODE_SELECTION)) {
                    Spinner spinnerFolder1 = dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_spinner);
                    folder1 = (Folder) spinnerFolder1.getSelectedItem();
                } else if (isFolderMode(1, MODE_CREATION)) {
                    String folder1Name = getFolderNameTextContent(1);
                    if (!folder1Name.isEmpty()) {
                        folder1 = new Folder(new Date(), folder0.getId(), folder1Name);
                        cardViewModel.createFolder(folder1);
                        addedFolderCard = true;
                        while (0 == folder1.getId()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        folder1Id = folder1.getId();
                    }
                }
            }

            if (null != folder1) {
                if (isFolderMode(2, MODE_SELECTION)) {
                    Spinner spinnerFolder2 = dialogView.findViewById(R.id.dialog_add_folder_card_folder_2_spinner);
                    folder2 = (Folder) spinnerFolder2.getSelectedItem();
                } else if (isFolderMode(2, MODE_CREATION)) {
                    String folder2Name = getFolderNameTextContent(2);
                    if (!folder2Name.isEmpty()) {
                        folder2 = new Folder(new Date(), folder1.getId(), folder2Name);
                        cardViewModel.createFolder(folder2);
                        addedFolderCard = true;
                        while (0 == folder2.getId()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        folder2Id = folder2.getId();
                    }
                }
            }

            String cardText1 = ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_text1_text)).getText().toString();
            String cardText2 = ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_text2_text)).getText().toString();
            if (!cardText1.isEmpty() && !cardText2.isEmpty()) {
                String cardName = ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_name_text)).getText().toString();
                if (cardName.isEmpty()) {
                    cardName = null;
                }
                Long folderId = null;
                if (null != folder0) {
                    if (null == folder1) {
                        folderId = folder0.getId();
                    } else {
                        if (null == folder2) {
                            folderId = folder1.getId();
                        } else {
                            folderId = folder2.getId();
                        }
                    }
                }
                Card card = new Card(new Date(), folderId, cardName, cardText1, cardText2);
                cardViewModel.createCard(card);
                addedFolderCard = true;
            }

            if (null != folder0Id) {
                ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_name_text)).setText("");
            }
            if (null != folder1Id) {
                ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_name_text)).setText("");
            }
            if (null != folder2Id) {
                ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_folder_2_name_text)).setText("");
            }
            ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_name_text)).setText("");
            ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_text1_text)).setText("");
            ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_card_text2_text)).setText("");

            if (null != folder0Id) {
                getFoldersByParentId(0, null);
            } else if (null != folder1Id) {
                assert folder0 != null;
                getFoldersByParentId(1, folder0.getId());
            } else if (null != folder2Id) {
                assert folder1 != null;
                getFoldersByParentId(2, folder1.getId());
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(activity, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    private void initFoldersModes() {
        folder0Modes = new ArrayList<>();
        folder0Modes.add(new FolderMode(MODE_NONE, "Aucun"));
        folder0Modes.add(new FolderMode(MODE_SELECTION, "Sélection"));
        folder0Modes.add(new FolderMode(MODE_CREATION, "Création"));

        folder1Modes = new ArrayList<>();
        folder1Modes.add(new FolderMode(MODE_NONE, "Aucun"));
        folder1Modes.add(new FolderMode(MODE_SELECTION, "Sélection"));
        folder1Modes.add(new FolderMode(MODE_CREATION, "Création"));

        folder2Modes = new ArrayList<>();
        folder2Modes.add(new FolderMode(MODE_NONE, "Aucun"));
        folder2Modes.add(new FolderMode(MODE_SELECTION, "Sélection"));
        folder2Modes.add(new FolderMode(MODE_CREATION, "Création"));

        updateFolderModes(0);
        updateFolderModes(1);
        updateFolderModes(2);
    }

    // region BindEvents
    private void bindEventsOnDialogElements(AlertDialog dialog) {
        Activity self = this;

        ((Spinner) dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_mode_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String folderMode = ((FolderMode) adapterView.getSelectedItem()).key;
                    switch (folderMode) {
                        case MODE_NONE:
                            onSelectedFolder0ModeNone();
                            break;
                        case MODE_SELECTION:
                            onSelectedFolder0ModeSelection();
                            break;
                        case MODE_CREATION:
                            onSelectedFolder0ModeCreation();
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ((Spinner) dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_mode_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String folderMode = ((FolderMode) adapterView.getSelectedItem()).key;
                    switch (folderMode) {
                        case MODE_NONE:
                            onSelectedFolder1ModeNone();
                            break;
                        case MODE_SELECTION:
                            onSelectedFolder1ModeSelection();
                            break;
                        case MODE_CREATION:
                            onSelectedFolder1ModeCreation();
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ((Spinner) dialogView.findViewById(R.id.dialog_add_folder_card_folder_2_mode_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    String folderMode = ((FolderMode) adapterView.getSelectedItem()).key;
                    switch (folderMode) {
                        case MODE_NONE:
                            onSelectedFolder2ModeNone();
                            break;
                        case MODE_SELECTION:
                            onSelectedFolder2ModeSelection();
                            break;
                        case MODE_CREATION:
                            onSelectedFolder2ModeCreation();
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((Spinner) dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    getFoldersByParentId(1, ((Folder) adapterView.getSelectedItem()).getId());
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ((Spinner) dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    getFoldersByParentId(2, ((Folder) adapterView.getSelectedItem()).getId());
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_name_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    onChangedFolder0NameText(editable);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
        ((EditText) dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_name_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    onChangedFolder1NameText(editable);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(self, getString(R.string.Error_occurred), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        });
    }

    private void onSelectedFolder0ModeNone() {
        toggleFolderSpinnerVisibility(0, false);
        toggleFolderNameTextVisibility(0, false);
        toggleFolderGroupVisibility(1, false);
        toggleFolderGroupVisibility(2, false);
    }

    private void onSelectedFolder0ModeSelection() {
        toggleFolderSpinnerVisibility(0, true);
        toggleFolderNameTextVisibility(0, false);
        toggleFolderModeSelectionVisibility(1, true);
        toggleFolderGroupVisibility(1, true);
        if (isFolderMode(1, MODE_SELECTION)) {
            toggleFolderModeSelectionVisibility(2, true);
            toggleFolderGroupVisibility(2, true);
        }
    }

    private void onSelectedFolder0ModeCreation() {
        toggleFolderSpinnerVisibility(0, false);
        toggleFolderNameTextVisibility(0, true);
        toggleFolderModeSelectionVisibility(1, false);
        toggleFolderMode(1, MODE_CREATION);
        toggleFolderSpinnerVisibility(1, false);
        toggleFolderNameTextVisibility(1, true);
        toggleFolderModeSelectionVisibility(2, false);
        toggleFolderMode(2, MODE_CREATION);
        toggleFolderSpinnerVisibility(2, false);
        toggleFolderNameTextVisibility(2, true);
        if (getFolderNameTextContent(0).isEmpty()) {
            toggleFolderGroupVisibility(1, false);
            toggleFolderGroupVisibility(2, false);
        } else {
            toggleFolderGroupVisibility(1, true);
            if (getFolderNameTextContent(1).isEmpty()) {
                toggleFolderGroupVisibility(2, false);
            } else {
                toggleFolderGroupVisibility(2, true);
            }
        }
    }

    private void onSelectedFolder1ModeNone() {
        toggleFolderSpinnerVisibility(1, false);
        toggleFolderNameTextVisibility(1, false);
        toggleFolderGroupVisibility(2, false);
    }

    private void onSelectedFolder1ModeSelection() {
        toggleFolderSpinnerVisibility(1, true);
        toggleFolderNameTextVisibility(1, false);
        toggleFolderModeSelectionVisibility(2, true);
        toggleFolderGroupVisibility(2, true);
    }

    private void onSelectedFolder1ModeCreation() {
        toggleFolderSpinnerVisibility(1, false);
        toggleFolderNameTextVisibility(1, true);
        toggleFolderModeSelectionVisibility(2, false);
        toggleFolderMode(2, MODE_CREATION);
        toggleFolderSpinnerVisibility(2, false);
        toggleFolderNameTextVisibility(2, true);
        if (getFolderNameTextContent(1).isEmpty()) {
            toggleFolderGroupVisibility(2, false);
        } else {
            toggleFolderGroupVisibility(2, true);
        }
    }

    private void onSelectedFolder2ModeNone() {
        toggleFolderSpinnerVisibility(2, false);
        toggleFolderNameTextVisibility(2, false);
    }

    private void onSelectedFolder2ModeSelection() {
        toggleFolderSpinnerVisibility(2, true);
        toggleFolderNameTextVisibility(2, false);
    }

    private void onSelectedFolder2ModeCreation() {
        toggleFolderSpinnerVisibility(2, false);
        toggleFolderNameTextVisibility(2, true);
    }

    private void onChangedFolder0NameText(@NonNull Editable editable) {
        if (editable.toString().isEmpty()) {
            toggleFolderGroupVisibility(1, false);
            toggleFolderGroupVisibility(2, false);
        } else {
            toggleFolderGroupVisibility(1, true);
            if (!getFolderNameTextContent(1).isEmpty()) {
                toggleFolderGroupVisibility(2, true);
            }
        }
    }

    private void onChangedFolder1NameText(@NonNull Editable editable) {
        toggleFolderGroupVisibility(2, !editable.toString().isEmpty());
    }
    // endregion

    // region Utils
    private void toggleFolderGroupVisibility(int folder, boolean show) throws IllegalArgumentException {
        int viewId;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_group;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_group;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_group;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }
        dialogView.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toggleFolderModeSelectionVisibility(int folder, boolean show) throws IllegalArgumentException {
        int viewId;
        List<FolderMode> folderModes;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_mode_spinner;
                folderModes = folder0Modes;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_mode_spinner;
                folderModes = folder1Modes;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_mode_spinner;
                folderModes = folder2Modes;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }

        Spinner spinner = dialogView.findViewById(viewId);
        int folderModesSelection = spinner.getSelectedItemPosition();

        Integer folderModeIndex = getFolderModeIndex(folderModes, MODE_SELECTION);
        if (show && null == folderModeIndex) {
            folderModes.add(1, new FolderMode(MODE_SELECTION, "Sélection"));
            updateFolderModes(folder);
            if (1 == folderModesSelection) {
                spinner.setSelection(2);
            }
        } else if (!show && null != folderModeIndex) {
            folderModes.remove(1);
            updateFolderModes(folder);
            if (1 == folderModesSelection) {
                spinner.setSelection(0);
            }
        }
    }

    private void toggleFolderSpinnerVisibility(int folder, boolean show) throws IllegalArgumentException {
        int viewId;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_spinner;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_spinner;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_spinner;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }
        dialogView.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toggleFolderNameTextVisibility(int folder, boolean show) throws IllegalArgumentException {
        int viewId;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_name_text;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_name_text;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_name_text;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }
        dialogView.findViewById(viewId).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toggleFolderMode(int folder, String mode) throws RuntimeException {
        int viewId;
        List<FolderMode> folderModes;
        boolean showSpinner, showNameText;

        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_mode_spinner;
                folderModes = folder0Modes;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_mode_spinner;
                folderModes = folder1Modes;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_mode_spinner;
                folderModes = folder2Modes;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }

        switch (mode) {
            case MODE_NONE:
                showSpinner = false;
                showNameText = false;
                break;
            case MODE_SELECTION:
                showSpinner = true;
                showNameText = false;
                break;
            case MODE_CREATION:
                showSpinner = false;
                showNameText = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }

        Integer folderModeIndex = getFolderModeIndex(folderModes, mode);
        if (null == folderModeIndex) {
            throw new RuntimeException("Tried to switch to mode " + mode + " for folder " + folder + " but it does not contain this mode");
        }

        Spinner spinner = dialogView.findViewById(viewId);
        spinner.setSelection(folderModeIndex);
        toggleFolderSpinnerVisibility(folder, showSpinner);
        toggleFolderNameTextVisibility(folder, showNameText);
    }

    @NonNull
    private String getFolderNameTextContent(int folder) throws IllegalArgumentException {
        int viewId;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_name_text;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_name_text;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_name_text;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }
        return ((EditText) dialogView.findViewById(viewId)).getText().toString();
    }

    @Nullable
    private Integer getFolderModeIndex(@NonNull List<FolderMode> folderModes, String mode) {
        for (int i = 0; i < folderModes.size(); i++) {
            if (mode.equals(folderModes.get(i).key)) {
                return i;
            }
        }
        return null;
    }

    private boolean isFolderMode(int folder, String mode) throws IllegalArgumentException {
        int viewId;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_mode_spinner;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_mode_spinner;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_mode_spinner;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }

        Spinner spinner = dialogView.findViewById(viewId);
        switch (mode) {
            case MODE_NONE:
                return MODE_NONE.equals(((FolderMode) spinner.getSelectedItem()).key);
            case MODE_SELECTION:
                return MODE_SELECTION.equals(((FolderMode) spinner.getSelectedItem()).key);
            case MODE_CREATION:
                return MODE_CREATION.equals(((FolderMode) spinner.getSelectedItem()).key);
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private void updateFolderModes(int folder) throws IllegalArgumentException {
        int viewId;
        List<FolderMode> folderModes;
        switch (folder) {
            case 0:
                viewId = R.id.dialog_add_folder_card_folder_0_mode_spinner;
                folderModes = folder0Modes;
                break;
            case 1:
                viewId = R.id.dialog_add_folder_card_folder_1_mode_spinner;
                folderModes = folder1Modes;
                break;
            case 2:
                viewId = R.id.dialog_add_folder_card_folder_2_mode_spinner;
                folderModes = folder2Modes;
                break;
            default:
                throw new IllegalArgumentException("Unknown folder: " + folder);
        }

        Spinner spinner = dialogView.findViewById(viewId);
        ArrayAdapter<FolderMode> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folderModes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void getFoldersByParentId(int level, @Nullable Long parentId) throws IllegalArgumentException {
        if (0 == level) {
            cardViewModel.getParentFolders().observe(this, this::updateDialogFolder0List);
        } else if (null != parentId) {
            switch (level) {
                case 1:
                    cardViewModel.getFoldersByParentId(parentId).observe(this, this::updateDialogFolder1List);
                    break;
                case 2:
                    cardViewModel.getFoldersByParentId(parentId).observe(this, this::updateDialogFolder2List);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected level: " + level);
            }
        } else {
            throw new IllegalArgumentException("Variable level is different from 0 but variable parentId is null");
        }
    }

    private void updateDialogFolder0List(@NonNull List<Folder> folders) {
        toggleFolderModeSelectionVisibility(0, true);
        toggleFolderMode(0, MODE_SELECTION);
        if (folders.isEmpty()) {
            folder0Id = null;
            toggleFolderModeSelectionVisibility(0, false);
            toggleFolderMode(0, MODE_CREATION);
            if (getFolderNameTextContent(0).isEmpty()) {
                toggleFolderGroupVisibility(1, false);
                if (getFolderNameTextContent(1).isEmpty()) {
                    toggleFolderGroupVisibility(2, false);
                }
            }
            return;
        }

        Spinner spinner = dialogView.findViewById(R.id.dialog_add_folder_card_folder_0_spinner);
        Folder selectedFolder = (Folder) spinner.getSelectedItem();
        ArrayAdapter<Folder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Integer selectedFolderPosition = null;
        Long selectedFolderId = null;
        if (null != folder0Id) {
            selectedFolderId = folder0Id;
        } else if (null != selectedFolder) {
            selectedFolderId = selectedFolder.getId();
        }
        if (null != selectedFolderId) {
            for (int i = 0; i < folders.size(); i++) {
                if (selectedFolderId == folders.get(i).getId()) {
                    selectedFolderPosition = i;
                    break;
                }
            }
        }
        if (null == selectedFolderPosition) {
            selectedFolderPosition = 0;
        }
        spinner.setSelection(selectedFolderPosition);
        folder0Id = null;
    }

    private void updateDialogFolder1List(@NonNull List<Folder> folders) {
        toggleFolderModeSelectionVisibility(1, true);
        toggleFolderMode(1, MODE_SELECTION);
        if (folders.isEmpty()) {
            folder1Id = null;
            toggleFolderModeSelectionVisibility(1, false);
            toggleFolderMode(1, MODE_CREATION);
            if (getFolderNameTextContent(1).isEmpty()) {
                toggleFolderGroupVisibility(2, false);
            }
            return;
        }
        toggleFolderGroupVisibility(1, true);

        Spinner spinner = dialogView.findViewById(R.id.dialog_add_folder_card_folder_1_spinner);
        Folder selectedFolder = (Folder) spinner.getSelectedItem();
        ArrayAdapter<Folder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Integer selectedFolderPosition = null;
        Long selectedFolderId = null;
        if (null != folder1Id) {
            selectedFolderId = folder1Id;
        } else if (null != selectedFolder) {
            selectedFolderId = selectedFolder.getId();
        }
        if (null != selectedFolderId) {
            for (int i = 0; i < folders.size(); i++) {
                if (selectedFolderId == folders.get(i).getId()) {
                    selectedFolderPosition = i;
                    break;
                }
            }
        }
        if (null == selectedFolderPosition) {
            selectedFolderPosition = 0;
        }
        spinner.setSelection(selectedFolderPosition);
        folder1Id = null;
    }

    private void updateDialogFolder2List(@NonNull List<Folder> folders) {
        toggleFolderModeSelectionVisibility(2, true);
        toggleFolderMode(2, MODE_SELECTION);
        if (folders.isEmpty()) {
            folder2Id = null;
            toggleFolderModeSelectionVisibility(2, false);
            toggleFolderMode(2, MODE_CREATION);
            return;
        }
        toggleFolderGroupVisibility(2, true);

        Spinner spinner = dialogView.findViewById(R.id.dialog_add_folder_card_folder_2_spinner);
        Folder selectedFolder = (Folder) spinner.getSelectedItem();
        ArrayAdapter<Folder> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, folders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Integer selectedFolderPosition = null;
        Long selectedFolderId = null;
        if (null != folder2Id) {
            selectedFolderId = folder2Id;
        } else if (null != selectedFolder) {
            selectedFolderId = selectedFolder.getId();
        }
        if (null != selectedFolderId) {
            for (int i = 0; i < folders.size(); i++) {
                if (selectedFolderId == folders.get(i).getId()) {
                    selectedFolderPosition = i;
                    break;
                }
            }
        }
        if (null == selectedFolderPosition) {
            selectedFolderPosition = 0;
        }
        spinner.setSelection(selectedFolderPosition);
        folder2Id = null;
    }
    // endregion
    // endregion
}
