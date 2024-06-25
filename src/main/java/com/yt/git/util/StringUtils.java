package com.yt.git.util;

public class StringUtils {

    public static boolean isBlank(String value){
        return value==null?true:"".equals(value.trim())?true:false;
    }

}
