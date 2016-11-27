package io.academic.service;


import io.academic.entity.DBArticle;
import io.academic.entity.OaiRecord;
import io.academic.entity.OaiRecordRepository;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.xml.DcXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Converts record to DC and saves as Arcticle
 */
@Service
public class DcService {


    Logger log = LoggerFactory.getLogger(ArticleService.class);

    public static String resumptionToken = "";

    @Autowired
    OaiRecordRepository oaiRecordRepository;

    @Autowired
    static
    ArticleService articleService;



    @Async
    public void add(OaiRecord oaiRecord) {
        oaiRecordRepository.save(oaiRecord);
        log.info("OAI saved in PostgreSQL with ID: {}", oaiRecord.getId());
    }

    @Async
    public static String getResumptionToken(String Link) throws IOException {

        OaiRecord oaiRecord  = new OaiRecord();
        Document doc = Jsoup.connect(Link).get();
        resumptionToken = doc.select("resumptionToken").first().ownText();
        oaiRecord.setToken(resumptionToken);
        return resumptionToken;

    }



    @Async
    public static void parse (String Link) throws TikaException, SAXException, IOException {

        Parser parser = new DcXMLParser();
        InputStream inputStream = new URL(Link).openStream();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        parser.parse(inputStream,handler,metadata,parseContext);

        DBArticle article = new DBArticle();

        article.setAuthors(metadata.get("author"));
        article.setId(Long.valueOf(metadata.get("identifier")));
        article.setTitle(metadata.get("title"));


        articleService.queue(article);

        inputStream.close();

    }



}
