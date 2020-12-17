package com.xjosiah.crawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;
import okhttp3.Request;

public class MyRequest extends OkHttpRequester {
    String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.67 Safari/537.36";

    @Override
    public Request.Builder createRequestBuilder(CrawlDatum crawlDatum) {
        return super.createRequestBuilder(crawlDatum)
                .addHeader("User-Agent", userAgent)
                .addHeader("method","get");
    }
}
