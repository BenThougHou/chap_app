package com.yiliao.dao.impl;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yiliao.dao.ReportDao;

public class ReportDaoImple implements ReportDao {

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List groupEvaluateKind(String kindid) {
		List list = null;
		try {
			list = jdbcTemplate
					.queryForList("SELECT S4.KINDNAME,S3.CLASSNAME,S2.STUDENTNAME,"
							+ "S1.LEARNMONTH,REPLACE(S1.APPRAISALCONTENT, 'rn', '') AS APPRAISALCONTENT,S2.STUDENTGUID "
							+ "FROM SYS_STUDENTLEARNING S1,SYS_STUDENT S2,SYS_CLASS S3,SYS_KINDMESSAGE S4 WHERE "
							+ "S1.STUDENTID = S2.STUDENTID AND S1.KINDID = S2.KINDID AND S1.CLASSID = S3.CLASSID AND "
							+ "S3.KINDID = S4.KINDID AND S2.KINDID = '"
							+ kindid + "' ORDER BY S3.CLASSID, S2.STUDENTGUID");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public List groupEvaluateStudent(String kindId, String learnId) {
		List list = null;
		String sql = "SELECT S1.LEARNMONTH,REPLACE(S1.APPRAISALCONTENT,'rn','')AS APPRAISALCONTENT,S2.STUDENTNAME,"
				+ "S3.CLASSNAME,S4.KINDNAME FROM SYS_STUDENTLEARNING S1,SYS_STUDENT S2,SYS_CLASS S3,SYS_KINDMESSAGE S4 "
				+ "WHERE S1.STUDENTID = S2.STUDENTID AND S1.KINDID = S2.KINDID AND S1.CLASSID = S3.CLASSID AND "
				+ "S3.KINDID = S4.KINDID AND S2.KINDID = '"
				+ kindId
				+ "' AND S1.LEARNID = '" + learnId + "'";
		try {
			list = jdbcTemplate.queryForList(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List groupEvaluateClass(String kindId, String classId) {
		List list = null;
		String sql = "SELECT S1.LEARNMONTH,REPLACE(S1.APPRAISALCONTENT,'rn','')AS APPRAISALCONTENT,S2.STUDENTNAME,"
				+ "S2.STUDENTGUID,S3.CLASSNAME,S4.KINDNAME FROM SYS_STUDENTLEARNING S1,SYS_STUDENT S2,SYS_CLASS S3,"
				+ "SYS_KINDMESSAGE S4 WHERE S1.STUDENTID = S2.STUDENTID AND S1.KINDID = S2.KINDID AND S1.CLASSID = S3.CLASSID "
				+ "AND S3.KINDID = S4.KINDID AND S2.KINDID = '"
				+ kindId
				+ "'AND S3.CLASSID= '" + classId + "'ORDER BY S2.STUDENTGUID";
		try {
			list = jdbcTemplate.queryForList(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List groupEvaluateStudentAll(String kindId, String classId,
			String studentId) {
		List list = null;
		String sql = "SELECT S4.KINDNAME,S3.CLASSNAME,S2.STUDENTNAME,S2.STUDENTGUID,S1.LEARNMONTH,"
				+ "REPLACE(S1.APPRAISALCONTENT, 'rn', '') AS APPRAISALCONTENT FROM SYS_STUDENTLEARNING S1,"
				+ "SYS_STUDENT S2,SYS_CLASS S3,SYS_KINDMESSAGE S4 WHERE S1.STUDENTID = S2.STUDENTID AND "
				+ "S1.KINDID = S2.KINDID AND S1.CLASSID = S3.CLASSID AND S3.KINDID = S4.KINDID AND "
				+ "S2.KINDID = '"
				+ kindId
				+ "' AND S3.CLASSID = '"
				+ classId
				+ "' AND "
				+ "S1.STUDENTID= '"
				+ studentId
				+ "' ORDER BY S2.STUDENTGUID";
		try {
			list = jdbcTemplate.queryForList(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
}
