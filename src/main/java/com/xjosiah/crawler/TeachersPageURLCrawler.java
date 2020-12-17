package com.xjosiah.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

import java.util.ArrayList;


public class TeachersPageURLCrawler extends BreadthCrawler {
    private ArrayList<String> pagesLink = new ArrayList<>();
    private String jsjGzhuUrl = "http://jsj.gzhu.edu.cn";

    public TeachersPageURLCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);

        addSeed(jsjGzhuUrl+"/szdw1/jsjkxywlgcxysz.htm");
        addSeed(jsjGzhuUrl+"/szdw1/wlkjxjjsyjysz.htm");
        addSeed(jsjGzhuUrl+"/szdw1/jskjyjysz.htm");

        setRequester(new MyRequest());
        setThreads(50);
        getConf().setTopN(100);
    }
    @Override
    public void visit(Page page, CrawlDatums crawlDatums) {
        Links links = page.links();
        Links filter = links.filterByRegex("http://jsj\\.gzhu\\.edu\\.cn/info/.*");
        for (String url : filter){
            pagesLink.add(url);
        }
    }

    public ArrayList<String> getPagesLink() {
        return pagesLink;
    }

    public static void main(String[] args) throws Exception {
        TeachersPageURLCrawler teachersPageURLCrawler = new TeachersPageURLCrawler("craw", true);
        teachersPageURLCrawler.start(1);
        ArrayList<String> pagesLink = teachersPageURLCrawler.getPagesLink();
        PageCrawler pageCrawler = new PageCrawler("craw", true, pagesLink);
        pageCrawler.start(1);
    }
}
