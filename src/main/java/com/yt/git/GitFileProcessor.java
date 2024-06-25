package com.yt.git;

import com.yt.git.config.ConfigProperties;
import com.yt.git.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class GitFileProcessor {

    private static Set<String> filePath=new HashSet<>();

    public static void findFile() throws Exception {
        System.out.println("==================从git中查找统计符合条件的提交信息==================");
        processor();
        String localFileCatalogue = ConfigProperties.getLocalFileCatalogue();
        System.out.println("=================开始找文件,并将文件复制至："+ localFileCatalogue);
        File file=new File(localFileCatalogue);
        if (!file.exists()){
            file.mkdirs();
        }
        try {
            Files.walkFileTree(Paths.get(ConfigProperties.getLocalGitPath()), new SimpleFileVisitor<Path>() {
                private long start;
                int count =0;
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toString().contains(".git")&&!file.toString().contains(".idea")){
                        if (filePath.contains(file.toString().replace("\\","/"))){
                            String path=localFileCatalogue+file.toString().replace("\\","/").replace(ConfigProperties.getLocalGitPath()+"/","").replace("/","#");
                            OutputStream out=new FileOutputStream(path);
                            Files.copy(file,out);
                            out.close();
                            System.out.println(file.getFileName());
                            System.out.println("File: " + file.toString());

                            count++;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.toString().replace("\\","/").equalsIgnoreCase(ConfigProperties.getLocalGitPath())){
                        start=System.currentTimeMillis();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (dir.toString().replace("\\","/").equalsIgnoreCase(ConfigProperties.getLocalGitPath())){
                        System.out.println("======共复制："+count+"  文件");
                        long end = System.currentTimeMillis();
                        System.out.println("共耗时（单位：秒）："+(end-start)/1000);

                        System.out.println("=============下载文件集合:");
                        String[] value=new String[filePath.size()];
                        List<String> objects = new ArrayList<>(Arrays.asList(filePath.toArray(value)));
                        Collections.sort(objects);
                        for (String object : objects) {
                            System.out.println(object);
                        }
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("=============程序执行结束===");
    }


    public static void processor()throws Exception{
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start =null;
        try {
            start=sdf.parse(ConfigProperties.getCommitStartTime());
        }catch (Exception e){

        }
        Date end=null;
        if (StringUtils.isBlank(ConfigProperties.getCommitEndTime())){
            end=new Date();
        }else {
            end=sdf.parse(ConfigProperties.getCommitEndTime());
        }
        Repository repository = Git.open(new File(ConfigProperties.getLocalGitPath())).getRepository();
        try {
            repository.resolve(ConfigProperties.getLocalGitBranch());
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> commits = git.log().call();
                for (RevCommit commit : commits) {
                    Date when = commit.getAuthorIdent().getWhen();
                    String name = commit.getAuthorIdent().getName();
                    String fullMessage = commit.getFullMessage();
                    if (start==null){
                        String format = sdf.format(when);
                        if (ConfigProperties.getCommitListTime().contains(format)){
                            doProcessor(name,fullMessage,sdf,commit,repository);
                        }
                    }else if (when.compareTo(start)==1&& //提交记录时间大于指定开始时间
                            when.compareTo(end)==-1&&//小于指定结束时间
                            !fullMessage.startsWith("Merge")){//merge内容不处理
                        doProcessor(name,fullMessage,sdf,commit,repository);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doProcessor(String name,String fullMessage,SimpleDateFormat sdf,RevCommit commit,Repository repository) throws IOException {
        if (!ConfigProperties.getUserList().isEmpty()){
            //TODO 指定只要某部分用户的commit内容
            if (ConfigProperties.getUserList().contains(name)){
                System.out.println("Message: " + fullMessage);
                System.out.println(""+"  "+sdf.format(commit.getAuthorIdent().getWhen())+"  "+ name +"  " + commit.getId().getName());
                printCommitFiles(repository, commit);
                System.out.println();
            }
        }else {
            //TODO 没有指定，要全部commit内容
            System.out.println("Message: " + fullMessage);
            System.out.println(""+"  "+sdf.format(commit.getAuthorIdent().getWhen())+"  "+ name +"  " + commit.getId().getName());
            printCommitFiles(repository, commit);
            System.out.println();
        }
    }

    private static   void printCommitFiles(Repository repository, RevCommit commit) throws IOException {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());
            try (DiffFormatter formatter = new DiffFormatter(new ByteArrayOutputStream())) {
                formatter.setRepository(repository);
                formatter.setContext(0);
                formatter.setPathFilter(null);

                // 比较两个提交之间的差异
                formatter.format(parent, commit);

                // 输出每个文件的变更信息
                for (DiffEntry entry : formatter.scan(parent, commit)) {
                    String newPath = entry.getNewPath();
                    System.out.println("文件路径" + newPath);
                    System.out.println("git 变更类型: " + entry.getChangeType());
                    List<String> skipFilePreFix = ConfigProperties.getSkipFilePreFix();
                    //前缀模糊匹配
                    boolean prefixBo=false;
                    if (skipFilePreFix !=null && skipFilePreFix.size()>0){
                        for (String filePreFix : skipFilePreFix) {
                            if (newPath.startsWith(filePreFix)){
                                prefixBo=true;
                                break;
                            }
                        }
                    }
                    // 正则表达式
                    boolean reBo=false;
                    List<String> regularExpression = ConfigProperties.getSkipFileRegularExpression();
                    if (regularExpression !=null && regularExpression.size()>0){
                        for (String regul : regularExpression) {
                            if (newPath.matches(regul)){
                                reBo=true;
                                break;
                            }
                        }
                    }
                    if (!(reBo||prefixBo)){
                        filePath.add(ConfigProperties.getLocalGitPath()+"/"+newPath.replace("\\","/"));
                    }
                }
            }
        }
    }
}
