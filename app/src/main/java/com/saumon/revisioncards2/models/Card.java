package com.saumon.revisioncards2.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(
            entity = Folder.class,
            parentColumns = "id",
            childColumns = "folderId",
            onDelete = CASCADE
        ),
        indices = {
            @Index(value = {"folderId", "creationDate"})
        }
)
public class Card {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Date creationDate;
    @Nullable
    private Long folderId;
    @Nullable
    private String name;
    private String text1;
    private String text2;
    private int sideToShow;

    @Ignore
    public Card(Date creationDate, @Nullable Long folderId, @Nullable String name, String text1, String text2, int sideToShow) {
        this.creationDate = creationDate;
        this.folderId = folderId;
        this.name = name;
        this.text1 = text1;
        this.text2 = text2;
        this.sideToShow = sideToShow;
    }

    public Card(Date creationDate, @Nullable Long folderId, @Nullable String name, String text1, String text2) {
        this.creationDate = creationDate;
        this.folderId = folderId;
        this.name = name;
        this.text1 = text1;
        this.text2 = text2;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Nullable
    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(@Nullable Long folderId) {
        this.folderId = folderId;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public int getSideToShow() {
        return sideToShow;
    }

    public void setSideToShow(int sideToShow) {
        this.sideToShow = sideToShow;
    }

    public String getTextToShow() {
        if (1 == sideToShow) {
            return text1;
        } else {
            return text2;
        }
    }

    public String getTextToHide() {
        if (1 == sideToShow) {
            return text2;
        } else {
            return text1;
        }
    }

    public void reverseSideToShow() {
        if (1 == sideToShow) {
            sideToShow = 2;
        } else {
            sideToShow = 1;
        }
    }
}
