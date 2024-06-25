package com.yt.git.config;

import com.yt.git.util.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigProperties {

    private static String localGitPath;// TODO 本地git 仓库地址
    private static String localGitBranch;// TODO 扫描的git分支
    private static String commitStartTime;//TODO 扫描的commit 时间范围的开始时间，如果为空，则认为未配置时间范围
    private static String commitEndTime;//TODO 扫描的commit 时间范围的截至时间，如果为空，取当前时间，
    private static List<String> commitListTime;// TODO 扫描指定时间的 commit 内容
    private static String localFileCatalogue;// TODO 复制的文件存放目录
    private static List<String> skipFilePreFix; // TODO 对处理每次commit 文件时，以这些为前缀的文件跳过
    private static List<String> skipFileRegularExpression;// TODO 对处理每次commit 文件时，符合这些正则表达式的文件跳过
    private static List<String> userList; // TODO 用户集合，只要这部分用户提交内容

    public static List<String> getUserList() {
        return userList==null?new ArrayList<>():userList;
    }

    public static void setUserList(List<String> userList) {
        ConfigProperties.userList = userList;
    }

    static {
        try (InputStream in=ConfigProperties.class.getClassLoader().getResourceAsStream("git_args.properties");
             InputStreamReader reader=new InputStreamReader(in,"UTF-8")
        ){
            Properties properties=new Properties();
            properties.load(reader);
            localGitPath = properties.getProperty("local.git.path");
            localGitBranch=properties.getProperty("local.git.branch");
            commitStartTime=properties.getProperty("commit.start.time");
            commitEndTime=properties.getProperty("commit.end.time");
            String property = properties.getProperty("commit.list.time");
            if (StringUtils.isBlank(commitStartTime)&&StringUtils.isBlank(commitEndTime)&&StringUtils.isBlank(property)){
                throw new Exception("commit.start.time ,commit.end.time 参数和 commit.list.time 参数不能都为空！");
            }
            if (!StringUtils.isBlank(property)){
                String[] split = property.split(",");
                commitListTime=new ArrayList<>(Arrays.asList(split));
            }
            localFileCatalogue=properties.getProperty("local.file.catalogue");
            String prefixList = properties.getProperty("skip.file.prefix");
            if (!StringUtils.isBlank(prefixList)){
                String[] split = prefixList.split(",");
                skipFilePreFix=new ArrayList<>(Arrays.asList(split));
            }
            String propertiesProperty = properties.getProperty("skip.file.regularExpression");
            if (!StringUtils.isBlank(propertiesProperty)){
                String[] split = propertiesProperty.split(",");
                skipFileRegularExpression=new ArrayList<>(Arrays.asList(split));
            }
            String userStr = properties.getProperty("commit.list.author");
            if (!StringUtils.isBlank(userStr)){
                String[] split = userStr.split(",");
                userList=new ArrayList<>(Arrays.asList(split));
            }
            // 打印属性值以验证
            System.out.println(" 打印属性值以验证:");
            properties.forEach((key, value) -> System.out.println(key + ": " + value));
            System.out.println("===========");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String getLocalGitPath() {
        return localGitPath;
    }

    public static void setLocalGitPath(String localGitPath) {
        ConfigProperties.localGitPath = localGitPath;
    }

    public static String getLocalGitBranch() {
        return localGitBranch;
    }

    public static void setLocalGitBranch(String localGitBranch) {
        ConfigProperties.localGitBranch = localGitBranch;
    }

    public static String getCommitStartTime() {
        return commitStartTime;
    }

    public static void setCommitStartTime(String commitStartTime) {
        ConfigProperties.commitStartTime = commitStartTime;
    }

    public static String getCommitEndTime() {
        return commitEndTime;
    }

    public static void setCommitEndTime(String commitEndTime) {
        ConfigProperties.commitEndTime = commitEndTime;
    }

    public static List<String> getCommitListTime() {
        return commitListTime==null?new ArrayList<>():commitListTime;
    }

    public static void setCommitListTime(List<String> commitListTime) {
        ConfigProperties.commitListTime = commitListTime;
    }

    public static String getLocalFileCatalogue() {
        return localFileCatalogue;
    }

    public static void setLocalFileCatalogue(String localFileCatalogue) {
        ConfigProperties.localFileCatalogue = localFileCatalogue;
    }

    public static List<String> getSkipFilePreFix() {
        return skipFilePreFix==null?new ArrayList<>():skipFilePreFix;
    }

    public static void setSkipFilePreFix(List<String> skipFilePreFix) {
        ConfigProperties.skipFilePreFix = skipFilePreFix;
    }

    public static List<String> getSkipFileRegularExpression() {
        return skipFileRegularExpression==null?new ArrayList<>():skipFileRegularExpression;
    }

    public static void setSkipFileRegularExpression(List<String> skipFileRegularExpression) {
        ConfigProperties.skipFileRegularExpression = skipFileRegularExpression;
    }
}
