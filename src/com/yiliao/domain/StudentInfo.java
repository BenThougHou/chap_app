package com.yiliao.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


/**
 * 学生信息实体类
 * 
 * @author LuoMeiling
 * @time 2012-03-23
 */
@Entity
@Table(name = "Sys_Student")
public class StudentInfo implements Serializable {

	private static final long serialVersionUID = 6541595170687850721L;

	@Id
	@GeneratedValue(generator = "paymentableGenerator")    
	@GenericGenerator(name = "paymentableGenerator", strategy = "uuid") 
	@Column(name="StudentGuid",length=32)
	private String m_strStudentGuid; //主键ID
	
	@Column(name="KindID",length=50)
	private String kindID;//幼儿园标识
	
	@Column(name="PartitionCode",length=32)
	private String partitionCode;//分区码
	
	@Column(name="StudentName",length=50)
	private String m_strStudentName; // 学生姓名
	
	@Column(name="StudentID",length=20)
	private String m_strStudentId;//学号
	
	@Column(name="Photo",length=200)
	private String m_strPhoto; // 学生照片
	
	@Column(name="Ethnic",length=50)
	private String m_strEthnic; // 民族
	
	@Column(name="Gender",length=50)
	private String m_strGender; // 性别
	
	@Column(name="Birthday")
	private String m_strBirthday; // 生日
	
	@Column(name="IDNumber",length=18)
	private String m_strIDNumber; // 身份证号码
	
	@Column(name="BirthPlace",length=100)
	private String m_strBirthPlace; // 出生地
	
	@Column(name="Address",length=200)
	private String m_strAddress; // 住址
	
	@Column(name="AccountType",length=50)
	private String m_strAccountType; // 户口性质
	
	@Column(name="JoinDate")
	private String m_strJoinDate; // 入园时间
	
	@Column(name="OnlyChild",length=50)
	private String m_strOnlyChild; // 是否独生子女
	
	@Column(name="SatyChild",length=50)
	private String m_strSatyChild; // 是否留守儿童
	
	@Column(name="SuiqianChild",length=50)
	private String m_strSuiqianChild; // 是否务工随迁儿童
	
	@Column(name="DisabilityChild",length=50)
	private String m_strDisabilityChild; // 是否残疾幼儿
	
	@Column(name="DisabilityType",length=500)
	private String m_strDisabilityType; // 残疾幼儿类别
	
	@Column(name="FullTime",length=50)
	private String m_strFullTime; // 全日制
	
	@Column(name="ClassID",length=32)
	private String m_strClassID; // 班级
	
	@Column(name="Is_Show",length=50)
	private Integer m_iIs_Show = 1; // 显示标识 默认为1显示 0为删除 2为离校
	
	@Column(name="Area",length=50)
	private String m_strArea; // 是否符合招生辖区

	@Column(name="Orphan",length=50)
	private String m_strOrphan; // 是否孤儿
	
	@Column(name="LowFamily",length=50)
	private String m_strLowFamily; // 是否低保家庭
	
	@Column(name="UrgentName",length=50)
	private String m_strUrgentName; // 紧急事件联系人
	
	@Column(name="URGENTNAMETWO",length=50)
	private String m_strUrgentNameTwo; // 第二位紧急联系人
	
	@Column(name="UrgentMobilePhone",length=11)
	private String m_strUrgentMobilePhone; // 紧急事件联系电话
	
	@Column(name="URGENTMOBILEPHONETWO",length=11)
	private String m_strUrgentMobilePhoneTwo; // 第二位紧急联系人号码

	@Column(name="Boarder",length=50)
	private String m_strBoarder; // 是否寄宿生
	
	@Column(name="Subsidize",length=50)
	private String m_strSubsidize; // 是否接受资助
	
	@Column(name="DocumentType",length=50)
	private String m_strDocumentType; // 幼儿证件类型
	
	@Column(name="Account",length=50)
	private String m_strAccount; // 幼儿户口所在地
	
	@Column(name="GuardianName",length=50)
	private String m_strGuardianName; // 监护人姓名
	
	@Column(name="GuardianDocumentType",length=50)
	private String m_strGuardianDocumentType; // 监护人身份证件类型
	
	@Column(name="GuardianDocumentNumber",length=50)
	private String m_strGuardianDocumentNumber; // 监护人身份证号码
	
	@Column(name="Nationality",length=50)
	private String m_strNationality; // 国籍
	
	@Column(name="Emigrant",length=50)
	private String m_strEmigrant; // 港澳台侨
	
	@Column(name="FamilyAccount",length=50)
	private String m_strFamilyAccount; // 家长账号（程序生成）
	
	@Column(name="FamilyPassword",length=50)
	private String m_strFamilyPassword; // 家长账号密码（生成简单密码）
	
	@Column(name="FAMILYACCOUNTFLAG",length=1)
	private Integer familyAccountFlag = 0;//账户修改标识；0表示提示（未修改状态下），1表示不再提示（已修改或者选择不再提示）

	@Column(name="ISFILTER")
	private Integer m_intIsFilter = 0; //0:不过滤，1过滤

	@Column(name="SMSNUMBYMONTH")
	private Integer m_iSmsNum=0; //该学生本月剩余短信条数
	
	@Column(name="IMEINUMBER")
	private String IMEINumber;//手表串号
	
	public String getM_strUrgentNameTwo() {
		return m_strUrgentNameTwo;
	}

	public void setM_strUrgentNameTwo(String mStrUrgentNameTwo) {
		m_strUrgentNameTwo = mStrUrgentNameTwo;
	}

	public String getM_strUrgentMobilePhoneTwo() {
		return m_strUrgentMobilePhoneTwo;
	}

	public void setM_strUrgentMobilePhoneTwo(String mStrUrgentMobilePhoneTwo) {
		m_strUrgentMobilePhoneTwo = mStrUrgentMobilePhoneTwo;
	}

	public Integer getM_iIs_Show() {
		return m_iIs_Show;
	}

	public void setM_iIs_Show(Integer mIIsShow) {
		m_iIs_Show = mIIsShow;
	}

	public Integer getFamilyAccountFlag() {
		return familyAccountFlag;
	}

	public void setFamilyAccountFlag(Integer familyAccountFlag) {
		this.familyAccountFlag = familyAccountFlag;
	}

	public Integer getM_intIsFilter() {
		return m_intIsFilter;
	}

	public void setM_intIsFilter(Integer mIntIsFilter) {
		m_intIsFilter = mIntIsFilter;
	}
	
	public String getM_strAddress() {
		return m_strAddress;
	}

	public void setM_strAddress(String mStrAddress) {
		m_strAddress = mStrAddress;
	}
	

	public String getM_strStudentGuid() {
		return m_strStudentGuid;
	}

	public void setM_strStudentGuid(String mStrStudentGuid) {
		m_strStudentGuid = mStrStudentGuid;
	}

	public String getM_strStudentName() {
		return m_strStudentName;
	}

	public void setM_strStudentName(String mStrStudentName) {
		m_strStudentName = mStrStudentName;
	}

	public String getM_strPhoto() {
		return m_strPhoto;
	}

	public void setM_strPhoto(String mStrPhoto) {
		m_strPhoto = mStrPhoto;
	}

	public String getM_strEthnic() {
		return m_strEthnic;
	}

	public void setM_strEthnic(String mStrEthnic) {
		m_strEthnic = mStrEthnic;
	}

	public String getM_strGender() {
		return m_strGender;
	}

	public void setM_strGender(String mStrGender) {
		m_strGender = mStrGender;
	}

	public String getM_strIDNumber() {
		return m_strIDNumber;
	}

	public void setM_strIDNumber(String mStrIDNumber) {
		m_strIDNumber = mStrIDNumber;
	}

	public String getM_strBirthPlace() {
		return m_strBirthPlace;
	}

	public void setM_strBirthPlace(String mStrBirthPlace) {
		m_strBirthPlace = mStrBirthPlace;
	}

	public String getM_strAccountType() {
		return m_strAccountType;
	}

	public void setM_strAccountType(String mStrAccountType) {
		m_strAccountType = mStrAccountType;
	}

	public String getM_strOnlyChild() {
		return m_strOnlyChild;
	}

	public void setM_strOnlyChild(String mStrOnlyChild) {
		m_strOnlyChild = mStrOnlyChild;
	}

	public String getM_strSatyChild() {
		return m_strSatyChild;
	}

	public void setM_strSatyChild(String mStrSatyChild) {
		m_strSatyChild = mStrSatyChild;
	}

	public String getM_strSuiqianChild() {
		return m_strSuiqianChild;
	}

	public void setM_strSuiqianChild(String mStrSuiqianChild) {
		m_strSuiqianChild = mStrSuiqianChild;
	}

	public String getM_strDisabilityChild() {
		return m_strDisabilityChild;
	}

	public void setM_strDisabilityChild(String mStrDisabilityChild) {
		m_strDisabilityChild = mStrDisabilityChild;
	}

	public String getM_strDisabilityType() {
		return m_strDisabilityType;
	}

	public void setM_strDisabilityType(String mStrDisabilityType) {
		m_strDisabilityType = mStrDisabilityType;
	}

	public String getM_strFullTime() {
		return m_strFullTime;
	}

	public void setM_strFullTime(String mStrFullTime) {
		m_strFullTime = mStrFullTime;
	}

	public String getM_strArea() {
		return m_strArea;
	}

	public void setM_strArea(String mStrArea) {
		m_strArea = mStrArea;
	}

	public String getM_strLowFamily() {
		return m_strLowFamily;
	}

	public void setM_strLowFamily(String mStrLowFamily) {
		m_strLowFamily = mStrLowFamily;
	}

	public String getM_strOrphan() {
		return m_strOrphan;
	}

	public void setM_strOrphan(String mStrOrphan) {
		m_strOrphan = mStrOrphan;
	}

	public String getM_strUrgentName() {
		return m_strUrgentName;
	}

	public void setM_strUrgentName(String mStrUrgentName) {
		m_strUrgentName = mStrUrgentName;
	}

	public String getM_strUrgentMobilePhone() {
		return m_strUrgentMobilePhone;
	}

	public void setM_strUrgentMobilePhone(String mStrUrgentMobilePhone) {
		m_strUrgentMobilePhone = mStrUrgentMobilePhone;
	}

	public String getM_strBoarder() {
		return m_strBoarder;
	}

	public void setM_strBoarder(String mStrBoarder) {
		m_strBoarder = mStrBoarder;
	}

	public String getM_strSubsidize() {
		return m_strSubsidize;
	}

	public void setM_strSubsidize(String mStrSubsidize) {
		m_strSubsidize = mStrSubsidize;
	}

	public String getM_strDocumentType() {
		return m_strDocumentType;
	}

	public void setM_strDocumentType(String mStrDocumentType) {
		m_strDocumentType = mStrDocumentType;
	}

	public String getM_strAccount() {
		return m_strAccount;
	}

	public void setM_strAccount(String mStrAccount) {
		m_strAccount = mStrAccount;
	}

	public String getM_strGuardianName() {
		return m_strGuardianName;
	}

	public void setM_strGuardianName(String mStrGuardianName) {
		m_strGuardianName = mStrGuardianName;
	}

	public String getM_strGuardianDocumentType() {
		return m_strGuardianDocumentType;
	}

	public void setM_strGuardianDocumentType(String mStrGuardianDocumentType) {
		m_strGuardianDocumentType = mStrGuardianDocumentType;
	}

	public String getM_strGuardianDocumentNumber() {
		return m_strGuardianDocumentNumber;
	}

	public void setM_strGuardianDocumentNumber(String mStrGuardianDocumentNumber) {
		m_strGuardianDocumentNumber = mStrGuardianDocumentNumber;
	}

	public String getM_strNationality() {
		return m_strNationality;
	}

	public void setM_strNationality(String mStrNationality) {
		m_strNationality = mStrNationality;
	}

	public String getM_strEmigrant() {
		return m_strEmigrant;
	}

	public void setM_strEmigrant(String mStrEmigrant) {
		m_strEmigrant = mStrEmigrant;
	}

	public String getM_strFamilyAccount() {
		return m_strFamilyAccount;
	}

	public void setM_strFamilyAccount(String mStrFamilyAccount) {
		m_strFamilyAccount = mStrFamilyAccount;
	}

	public String getM_strFamilyPassword() {
		return m_strFamilyPassword;
	}

	public void setM_strFamilyPassword(String mStrFamilyPassword) {
		m_strFamilyPassword = mStrFamilyPassword;
	}

	public String getKindID() {
		return kindID;
	}

	public void setKindID(String kindID) {
		this.kindID = kindID;
	}

	public String getPartitionCode() {
		return partitionCode;
	}

	public void setPartitionCode(String partitionCode) {
		this.partitionCode = partitionCode;
	}

	public String getM_strStudentId() {
		return m_strStudentId;
	}

	public void setM_strStudentId(String mStrStudentId) {
		m_strStudentId = mStrStudentId;
	}

	public String getM_strBirthday() {
		return m_strBirthday;
	}

	public void setM_strBirthday(String mStrBirthday) {
		m_strBirthday = mStrBirthday;
	}

	public String getM_strJoinDate() {
		return m_strJoinDate;
	}

	public void setM_strJoinDate(String mStrJoinDate) {
		m_strJoinDate = mStrJoinDate;
	}

	public String getM_strClassID() {
		return m_strClassID;
	}

	public void setM_strClassID(String mStrClassID) {
		m_strClassID = mStrClassID;
	}

	public Integer getM_iSmsNum() {
		return m_iSmsNum;
	}

	public void setM_iSmsNum(Integer mISmsNum) {
		m_iSmsNum = mISmsNum;
	}

	public String getIMEINumber() {
		return IMEINumber;
	}

	public void setIMEINumber(String iMEINumber) {
		IMEINumber = iMEINumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
