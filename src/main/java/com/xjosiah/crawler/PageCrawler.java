package com.xjosiah.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.xjosiah.crawler.beans.Teacher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class PageCrawler extends BreadthCrawler {

    public PageCrawler(String crawlPath, boolean autoParse, ArrayList<String> pagesUrl) {
        super(crawlPath, autoParse);

        for (String pageUrl : pagesUrl) {
            addSeed(pageUrl);
        }

        setRequester(new MyRequest());
        setThreads(50);
        getConf().setTopN(100);
    }

    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        ArrayList<String> msgList = new ArrayList<>();
        String name = page.select("div .contain h2").text();
        msgList.add(name);
//        System.out.println(name);

        String picURL = "http://jsj.gzhu.edu.cn" + page.select("div #vsb_content p img").attr("src");
        msgList.add(picURL);
//        System.out.println(picURL);

        Elements teacherMsg = page.select("div #vsb_content p");
        for (Element e : teacherMsg) {
            msgList.add(e.text());
        }
        tackleMsg(msgList);
    }

    private void tackleMsg(ArrayList<String> msgList) {
        Teacher teacher = new Teacher();
        String name = msgList.get(0);
        teacher.setName(name);
        msgList.remove(0);

        String teacherPicURL = msgList.get(0);
        msgList.remove(0);

        boolean savaImgSuccess = getTeacherPic(name,teacherPicURL);
        if (savaImgSuccess)
            //  不为空则toString方法可以区分有无数据
            teacher.setPic(new byte[0]);
        for (int i = 0; i < msgList.size(); i++) {
            String msg = msgList.get(i);
            System.out.println("[TackleMsg]catch msg:\t" + msg);
            if (!msg.contains("：")) {
                System.out.println("is null skip");
                continue;
            } else if (msg.endsWith("：")) {
                i += 1;
                msg += msgList.get(i);
            }
            if (msgList.size() != 0) {
                setTeacherInfo(teacher, msg);
            }
        }
        try {
            FileWriter writer = new FileWriter(new File("teacher_msg/" + name + ".txt"));
            writer.write(teacher.toString());
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean getTeacherPic(String name,String teacherPicURL){
        try {
            URL picUrl = new URL(teacherPicURL);
            URLConnection urlConnection = picUrl.openConnection();
            urlConnection.setDoInput(true);
            byte[] buffer = new byte[1024];
            int len;
            FileOutputStream fileOutputStream = new FileOutputStream(new File("teacher_images/"+name+".jpg"));
            InputStream reader = urlConnection.getInputStream();
            while((len=reader.read(buffer))!=-1){
                fileOutputStream.write(buffer,0,len);
                fileOutputStream.flush();
                buffer = new byte[1024];
            }
            return true;
        } catch (MalformedURLException e) {
            System.err.println(name+"\t图片资源不存在！URL:"+ teacherPicURL);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("图片流存储失败");
            e.printStackTrace();
        }
        return false;
    }

    private void setTeacherInfo(Teacher teacher, String msg) {
        String[] split = msg.split("：");
        String attr = split[0];
        String value = null;
        try {
            value = split[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println(teacher.getName() + "\t该名教师的信息格式有错误，请重新查找处理！");
        }
        switch (attr) {
            case "职称":
                teacher.setTitle(value);
                break;
            case "部门":
            case "系、研究所":
                teacher.setResearchInstitute(value);
                break;
            case "研究领域":
                teacher.setSpecialism(value);
                break;
            case "讲述课程":
            case "讲授课程":
                teacher.setCourse(value);
                break;
            case "电子邮箱":
            case "电子邮件":
            case "个人邮箱":
                teacher.setEmail(value);
                break;
            case "办公地点":
                teacher.setOffice(value);
                break;
            case "个人主页":
                try {
                    teacher.setPage(new URL(value));
                } catch (MalformedURLException e) {
                    System.err.println(value + "\t该URL已经失效");
                }
                break;
            case "办公电话":
                teacher.setPhoneNum(value);
                break;
            default:
                System.err.println(split[0] + "该属性未被处理，请重新处理！");
        }
    }

//    public static void main(String[] args) throws Exception {
//        ArrayList<String> pagesUrl = new ArrayList<>();
//        pagesUrl.add("http://jsj.gzhu.edu.cn/info/1227/2254.htm");
//        PageCrawler craw = new PageCrawler("craw", true, pagesUrl);
//        craw.start(1);
//    }
}
