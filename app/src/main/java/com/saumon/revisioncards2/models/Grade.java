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
            entity = Card.class,
            parentColumns = "id",
            childColumns = "cardId",
            onDelete = CASCADE
        ),
        indices = {
            @Index(value = {"cardId", "creationDate"})
        }
)
public class Grade {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private Date creationDate;
    private Long cardId;
    private int value;

    public Grade(Date creationDate, Long cardId, int value) {
        this.creationDate = creationDate;
        this.cardId = cardId;
        this.value = value;
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

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
