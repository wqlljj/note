package com.cloudminds.meta.util;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SX on 2018/2/22.
 */

public class StringUtils {
    public static final int TYPE_CHINESE=1;
    public static final int TYPE_ENGLISH=2;
    public static final int TYPE_NUM=3;
    public static final int TYPE_SYMBOL =0;
    static String TAG="StringSpiltByLanguage";
    static String chineseRegex="[\\u4e00-\\u9fa5]";
    static String englishRegex="[a-zA-Z]";
    static String numRegex="[0-9]";
    public static int isNameQualified(String str) {
        Log.e(TAG, "isQualified: 0" );
        String regex_2="(([\u4e00-\u9fa5]+[ .]?)|([0-9]+[ .]?)|([a-zA-Z]+[ .]?))*";
        String regex_1="^([\u4e00-\u9fa5]|[0-9]|[a-zA-Z]).*([\u4e00-\u9fa5]|[0-9]|[a-zA-Z])$";
        if (!str.matches(regex_1)) {
            Log.e(TAG, "isQualified: 不和规则1" );
            return 1;
        }else if (!str.matches(regex_2)) {
            Log.e(TAG, "isQualified: 不和规则2" );
            return 2;
        }else{
            Log.e(TAG, "isQualified: 1" );
            int ch_num = StringUtils.statistic(str, StringUtils.TYPE_CHINESE);
            int en_num = StringUtils.statistic(str, StringUtils.TYPE_ENGLISH);
            int nu_num = StringUtils.statistic(str, StringUtils.TYPE_NUM);
            Log.e(TAG, "isQualified: "+ch_num+"  "+en_num+"  "+nu_num );
            if(ch_num >10){
                Log.e(TAG, "isQualified: 中文过10" );
                return 3;
            }else if(ch_num>0&&(en_num+nu_num>20||str.length()>30)){
                Log.e(TAG, "isQualified: 混合英文过20或总长过30" );
                return str.length()<=30?4:5;
            }else if(ch_num==0&&(en_num+nu_num>30||str.length()>30)){
                Log.e(TAG, "isQualified: 英文过30或总长过30" );
                return str.length()<=30?6:5;
            }
        }
        Log.e(TAG, "isQualified: 合格" );
        return 0;
    }
    //分离中英数，顺序不变
    public static ArrayList<String> split(String content){
        int wordType=0;
        ArrayList<String> result = new ArrayList<>();
        Log.e(TAG, "split: "+content );
        if(TextUtils.isEmpty(content)){
            return result;
        }
        int length = content.length();
        String temp="";
        for (int i = 0; i < length;) {
            int type = stratType(content);
            if(wordType!=type){
                if(wordType==0){
                    wordType=type;
                }else if(type!=0){
                    result.add(temp);
                    temp="";
                    wordType=type;
                }
            }
            int len=0;
            Log.e(TAG, "split: "+type );
            switch (type){
                case TYPE_SYMBOL:
                    len=1;
                    if(len!=0){
                        temp+=content.substring(0,len);
                        content=content.substring(len);
                    }
                    break;
                case TYPE_CHINESE:
                    len=wordLength(content,"^"+chineseRegex+"+");
                    if(len!=0){
                        temp+=content.substring(0,len);
                        content=content.substring(len);
                    }
                    break;
                case TYPE_ENGLISH:
                    len=wordLength(content,"^"+englishRegex+"+");
                    if(len!=0){
                        temp+=content.substring(0,len);
                        content=content.substring(len);
                    }
                    break;
                case TYPE_NUM:
                    len=wordLength(content,"^"+numRegex+"+");
                    if(len!=0){
                        temp+=content.substring(0,len);
                        content=content.substring(len);
                    }
                    break;
            }
            Log.e(TAG, "split: "+temp );
            i+=len;
        }
        result.add(temp);
        Log.e(TAG, "split: result = "+result );
        return result;
    }
    //统计中，英，数的个数
    public static int statistic(String content,int type){
        int result=0;
        if(TextUtils.isEmpty(content)){
            return result;
        }
        int length = content.length();
         for (int i = 0; i < length;) {
                    int now_type = stratType(content);
                    int len=0;
                    if(now_type!=type){
                        Log.e(TAG, "split: "+type );
                        switch (now_type){
                            case TYPE_SYMBOL:
                                len=1;
                                if(len!=0){
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_CHINESE:
                                len=wordLength(content,"^"+chineseRegex+"+");
                                if(len!=0){
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_ENGLISH:
                                len=wordLength(content,"^"+englishRegex+"+");
                                if(len!=0){
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_NUM:
                                len=wordLength(content,"^"+numRegex+"+");
                                if(len!=0){
                                    content=content.substring(len);
                                }
                                break;
                        }
                    }else{
                        switch (now_type){
                            case TYPE_SYMBOL:
                                len=1;
                                if(len!=0){
                                    result+=len;
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_CHINESE:
                                len=wordLength(content,"^"+chineseRegex+"+");
                                if(len!=0){
                                    result+=len;
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_ENGLISH:
                                len=wordLength(content,"^"+englishRegex+"+");
                                if(len!=0){
                                    result+=len;
                                    content=content.substring(len);
                                }
                                break;
                            case TYPE_NUM:
                                len=wordLength(content,"^"+numRegex+"+");
                                if(len!=0){
                                    result+=len;
                                    content=content.substring(len);
                                }
                                break;
                        }
                    }
                    i+=len;
                }
        return result;
    }
    //字符串起始类型长度
    private static int wordLength(String word, String regex){
        int length=0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(word);
        if(matcher.find()){
            String group = matcher.group();
            length=group.length();
            Log.e(TAG, "wordLength: "+group );
        }
        return length;
    }
    //字符串起始字符类型（中，英，数，符号）判断
    private static int stratType(String content){
        if(content.matches("^"+chineseRegex+"(.*\\n?)*"))
            return TYPE_CHINESE;
        else if(content.matches("^"+englishRegex+"(.*\\n?)*"))
            return TYPE_ENGLISH;
        else if(content.matches("^"+numRegex+"(.*\\n?)*"))
            return TYPE_NUM;
        else
            return TYPE_SYMBOL;
    }
}
