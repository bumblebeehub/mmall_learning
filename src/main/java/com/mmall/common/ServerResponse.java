package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by mjl on 18-7-15.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//上面的注解，比如说只返回一个status给前端时，msg和data默认为一个值为空的键，当序列化的时候，就不会出现空的这些字段了

public class ServerResponse<T> implements Serializable{
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status)
    {
        this.status = status;
    }

    private ServerResponse(int status, T data)
    {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg, T data)
    {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int status, String msg)
    {
        this.status = status;
        this.msg = msg;
    }
    //当返回的数据是String类型时，要如何保证serviceresponse自己调用合适的构造器而不是第四个
    //构造器这样直接把string作为msg返回了

    @JsonIgnore //使之不在json序列化结果当中

    public boolean isSuccess()
    {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus()
    {
        return status;
    }

    public T getData()
    {
        return data;
    }

    public String getMsg()
    {
        return msg;
    }
    public static <T> ServerResponse<T> createBySuccess()
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    //由开放的返回方法可以看到，当String作为data返回时，会自动调用正确的方法

    public static <T> ServerResponse<T> createByError()
    {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage)
    {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage)
    {
        return new ServerResponse<T>(errorCode, errorMessage);
    }
}
