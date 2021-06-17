package com.yiliao.domain.entity;

/**
 * 实名验证
 */
public class Cretification {

    /**
     * id
     */
    private int t_id;

    /**
     * 用户id
     */
    private int t_user_id;

    /**
     * 用户照片
     */
    private String t_user_photo;

    /**
     * 用户视频
     */
    private String t_user_video;

    /**
     * 用户姓名
     */
    private String t_nam;

    /**
     * 身份证
     */
    private String t_id_card;

    /**
     * 审核状态
     * 0.未审核
     * 1.审核成功
     * 2.审核失败
     */
    private int t_certification_type;

    /**
     * 描述
     */
    private String t_describe;

    /**
     * 添加时间
     */
    private String t_create_time;

    /**
     * 身份证正面
     */
    private String t_card_back;

    /**
     * 身份证反面
     */
    private String t_card_face;

    /**
     * 验证类型 0：视频 1：手机 2：身份证
     */
    private int t_type;

    public int getT_id() {
        return t_id;
    }

    public void setT_id(int t_id) {
        this.t_id = t_id;
    }

    public int getT_user_id() {
        return t_user_id;
    }

    public void setT_user_id(int t_user_id) {
        this.t_user_id = t_user_id;
    }

    public String getT_user_photo() {
        return t_user_photo;
    }

    public void setT_user_photo(String t_user_photo) {
        this.t_user_photo = t_user_photo;
    }

    public String getT_user_video() {
        return t_user_video;
    }

    public void setT_user_video(String t_user_video) {
        this.t_user_video = t_user_video;
    }

    public String getT_nam() {
        return t_nam;
    }

    public void setT_nam(String t_nam) {
        this.t_nam = t_nam;
    }

    public String getT_id_card() {
        return t_id_card;
    }

    public void setT_id_card(String t_id_card) {
        this.t_id_card = t_id_card;
    }

    public int getT_certification_type() {
        return t_certification_type;
    }

    public void setT_certification_type(int t_certification_type) {
        this.t_certification_type = t_certification_type;
    }

    public String getT_describe() {
        return t_describe;
    }

    public void setT_describe(String t_describe) {
        this.t_describe = t_describe;
    }

    public String getT_create_time() {
        return t_create_time;
    }

    public void setT_create_time(String t_create_time) {
        this.t_create_time = t_create_time;
    }

    public String getT_card_back() {
        return t_card_back;
    }

    public void setT_card_back(String t_card_back) {
        this.t_card_back = t_card_back;
    }

    public String getT_card_face() {
        return t_card_face;
    }

    public void setT_card_face(String t_card_face) {
        this.t_card_face = t_card_face;
    }

    public int getT_type() {
        return t_type;
    }

    public void setT_type(int t_type) {
        this.t_type = t_type;
    }
}
