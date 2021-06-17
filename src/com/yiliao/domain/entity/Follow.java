package com.yiliao.domain.entity;

import com.yiliao.util.DateUtils;

import java.util.Date;

/**
 * 关注
 */
public class Follow {
    /**
     * id
     */
    private int t_id;

    /**
     * 关注人id
     */
    private int t_follow_id;

    /**
     * 被关注人id
     */
    private int t_cover_follow;

    /**
     * 关注时间
     */
    private String t_create_time;

    /**
     * 关注类型 0谁喜欢我    1我喜欢谁     2互相喜欢
     */
    private int type;

    /**
     * 是否已读 0未读 1已读
     */
    private  int t_is_read;

    public int getT_id() {
        return t_id;
    }

    public void setT_id(int t_id) {
        this.t_id = t_id;
    }

    public int getT_follow_id() {
        return t_follow_id;
    }

    public void setT_follow_id(int t_follow_id) {
        this.t_follow_id = t_follow_id;
    }

    public int getT_cover_follow() {
        return t_cover_follow;
    }

    public void setT_cover_follow(int t_cover_follow) {
        this.t_cover_follow = t_cover_follow;
    }

    public String getT_create_time() {
        return t_create_time;
    }

    public void setT_create_time(String t_create_time) {
        this.t_create_time = t_create_time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getT_is_read() {
        return t_is_read;
    }

    public void setT_is_read(int t_is_read) {
        this.t_is_read = t_is_read;
    }
}
