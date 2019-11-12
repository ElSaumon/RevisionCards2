package com.saumon.revisioncards2.repositories;

import androidx.lifecycle.LiveData;

import com.saumon.revisioncards2.database.dao.GradeDao;
import com.saumon.revisioncards2.models.Grade;

import java.util.List;

public class GradeDataRepository {
    private final GradeDao gradeDao;

    public GradeDataRepository(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

    public LiveData<List<Grade>> getGrades() {
        return gradeDao.getGrades();
    }

    public List<Grade> getGradesFromCard(long cardId) {
        return gradeDao.getGradesFromCard(cardId);
    }

    public void createGrade(Grade grade) {
        long gradeId = gradeDao.insertGrade(grade);
        grade.setId(gradeId);
    }

    public void updateGrade(Grade grade) {
        gradeDao.updateGrade(grade);
    }

    public void deleteGrade(Grade grade) {
        gradeDao.deleteGrade(grade);
    }
}
