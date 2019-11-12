package com.saumon.revisioncards2.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    foreignKeys = @ForeignKey(
        entity = Folder.class,
        parentColumns = "id",
        childColumns = "parentId",
        onDelete = CASCADE
    ), indices = {
        @Index(value = {"parentId", "creationDate"})
    }
)
public class Folder {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Date creationDate;
    @Nullable
    private Long parentId;
    private String name;

    public Folder(Date creationDate, @Nullable Long parentId, String name) {
        this.creationDate = creationDate;
        this.parentId = parentId;
        this.name = name;
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
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
