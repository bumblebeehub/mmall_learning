package com.mmall.common;

/**
 * Created by mjl on 18-7-15.
 */
public class Const {
    public static final String CURRENT_USER = "curren_user";

    public static final String EMAIL= "email";
    public static final String USERNAME = "username";
    //对用户进行分组
    public interface Role
    {
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1; //管理员
    }
}
