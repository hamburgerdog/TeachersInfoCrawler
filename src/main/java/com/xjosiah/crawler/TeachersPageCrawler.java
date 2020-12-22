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

/**
 * 爬取http://jsj.gzhu.edu.cn的学院教师信息
 *
 * @author xjosiah
 * @since 2020.12.22
 */
public class TeachersPageCrawler extends BreadthCrawler {
    private String jsjGzhuUrl = "http://jsj.gzhu.edu.cn";

    public TeachersPageCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);

        //  初始化种子
        addSeed(jsjGzhuUrl + "/szdw1/jsjkxywlgcxysz.htm");
        addSeed(jsjGzhuUrl + "/szdw1/wlkjxjjsyjysz.htm");
        addSeed(jsjGzhuUrl + "/szdw1/jskjyjysz.htm");
        //  过滤第一层爬取得到的URL，筛选出可用部分
        addRegex("http://jsj\\.gzhu\\.edu\\.cn/info/.*");
        //  设置请求头，模拟浏览器请求
        //  Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1)
        //  AppleWebKit/537.36 (KHTML, like Gecko)
        //  Chrome/87.0.4280.67 Safari/537.36
        setRequester(new MyRequest());

        //  设置可以用线程数量
        setThreads(50);
        getConf().setTopN(100);
    }

    /**
     * 每一次爬取到对应的网页后自动进行的分析
     * <p>
     * 如果经过URL判断后，不是所需信息页会直接返回
     * webcollector框架底层使用的是jsoup来分析HTML文档，如select()方法返回的就是 org.jsoup.select.Elements
     *
     * @param page
     * @param crawlDatums
     */
    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        if (!page.url().matches("http://jsj\\.gzhu\\.edu\\.cn/info/.*"))
            return;
        //  存放信息的集合
        ArrayList<String> msgList = new ArrayList<>();

        String name = page.select("div .contain h2").text();
        msgList.add(name);

        String picURL = "http://jsj.gzhu.edu.cn" + page.select("div #vsb_content p img").attr("src");
        msgList.add(picURL);

        Elements teacherMsg = page.select("div #vsb_content p");
        //  从P标签中获取详细信息：讲授课程、个人邮箱、办公地址等.....
        for (Element e : teacherMsg) {
            msgList.add(e.text());
        }
        tackleMsg(msgList);
    }

    /**
     * 读取信息集合，创建教师对象，保存教师信息
     *
     * @param msgList
     */
    private void tackleMsg(ArrayList<String> msgList) {
        Teacher teacher = new Teacher();

        //  name和url下标不会变，总在0和1，不论有无
        String name = msgList.get(0);
        teacher.setName(name);
        msgList.remove(0);

        String teacherPicURL = msgList.get(0);
        msgList.remove(0);

        //  通过图片URL下载保存图片
        boolean savaImgSuccess = getTeacherPic(name, teacherPicURL);
        /*
            不会放入具体图片的数据到教师对象中
            放入空数组是为了和教师有无图片进行区分，没有图片则teacher.pic==null
            用于toString（）方法中进行判断方便查看，具体存放位置在项目文件中
        */
        if (savaImgSuccess) {
            teacher.setPic(new byte[0]);
        }
        //  处理P标签中的信息，网页中使用 '：' 做为分割符
        //  这里主要做不符合格式信息过滤，具体分析过程在setTeacherInfo()中
        for (int i = 0; i < msgList.size(); i++) {
            String msg = msgList.get(i);
//            System.out.println("[TackleMsg]catch msg:\t" + msg);
            if (!msg.contains("：")) {
//                System.out.println("is null skip");
                continue;
            } else if (msg.endsWith("：")) {
                i += 1;
                msg += msgList.get(i);
            }
            if (msgList.size() != 0) {
                setTeacherInfo(teacher, msg);
            }
        }
        System.out.println(teacher);
        //  持久层操作：直接将对象写入到txt文档中，也可以用json、java对象序列化等等不再赘述
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

    /**
     * 下载教师图片
     * @param name              教师名
     * @param teacherPicURL     教师相片URL
     * @return                  能否成功获取到图片
     */
    private boolean getTeacherPic(String name, String teacherPicURL) {
        try {
            URL picUrl = new URL(teacherPicURL);
            URLConnection urlConnection = picUrl.openConnection();
            //  开启读取模式
            urlConnection.setDoInput(true);
            byte[] buffer = new byte[1024];
            int len;
            FileOutputStream fileOutputStream = new FileOutputStream(new File("teacher_images/" + name + ".jpg"));
            InputStream reader = urlConnection.getInputStream();
            while ((len = reader.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
                fileOutputStream.flush();
                buffer = new byte[1024];
            }
            return true;
        } catch (MalformedURLException e) {
            System.err.println(name + "\t图片资源不存在！URL:" + teacherPicURL);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("图片流存储失败");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 分析信息集合，找出教师具体信息
     * @param teacher   教师对象
     * @param msg       教师某一项具体信息，格式和json类似为（key：value）
     */
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

    public static void main(String[] args) throws Exception {
        TeachersPageCrawler teachersPageURLCrawler = new TeachersPageCrawler("craw", true);
        teachersPageURLCrawler.start(3);
    }
}
