package com.yiliao.dao;

import java.util.List;

public interface ReportDao {
	public List groupEvaluateKind(String kindId);

	public List groupEvaluateStudent(String kindId, String learnId);

	public List groupEvaluateClass(String kindId, String classId);
	
	public List groupEvaluateStudentAll(String kindId,String classId,String studentId);
}
