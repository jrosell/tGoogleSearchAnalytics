package com.thediar.talendcomp.googlesearchanalytics.source;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.google.api.services.webmasters.model.*;
import com.thediar.talendcomp.googlesearchanalytics.datastore.CustomDatastore;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.input.Producer;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import com.thediar.talendcomp.googlesearchanalytics.service.ThediarTalendcompGooglesearchanalyticsService;
import com.thediar.talendcomp.googlesearchanalytics.dataset.CustomDataset;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Clock;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

import com.google.api.services.webmasters.Webmasters;
import com.google.api.services.webmasters.WebmastersScopes;
import com.google.api.services.webmasters.Webmasters.Searchanalytics.Query;
import com.google.api.services.webmasters.model.SearchAnalyticsQueryRequest;
import com.google.api.services.webmasters.model.SearchAnalyticsQueryResponse;

@Documentation("TODO fill the documentation for this source")
public class InputSource implements Serializable {
    private final InputMapperConfiguration configuration;
    private final ThediarTalendcompGooglesearchanalyticsService service;
    private final RecordBuilderFactory builderFactory;
    private final CustomDataset dataset;
    private final CustomDatastore datastore;

    private Logger logger = null;
    private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private final JsonFactory JSON_FACTORY = new JacksonFactory();
    private File keyFile; // *.p12 key file is needed
    private String accountEmail;
    private int timeoutInSeconds = 120;
    private long timeMillisOffsetToPast = 10000;
    private String siteUrl = null;
    private String startDate = null;
    private String endDate = null;
    private SearchAnalyticsQueryRequest request;
    private SearchAnalyticsQueryResponse response;
    private String dimensions = null;
    private List<String> dimensionsList = null;
    private String searchType = null;
    private String dimensionsFilterGroups = null;
    private List<ApiDimensionFilterGroup> apiDimensionFilterGroups = null;
    private String aggregationType = null;
    private Integer startRow = null;
    private Integer rowLimit = null;
    private Webmasters webmastersService;
    private boolean debug = false;
    private int max = 1;
    private int iterator = 0;

    public InputSource(@Option("configuration") final InputMapperConfiguration configuration,
                        final ThediarTalendcompGooglesearchanalyticsService service,
                        final RecordBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
        this.dataset = this.configuration.getDataset();
        this.datastore = this.dataset.getDatastore();
    }

    @PostConstruct
    public void init() {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        try{
            setAccountEmail(datastore.getService_account_email());
            setKeyFile(datastore.getKeyfile());
            setTimeoutInSeconds(240);
            setTimeMillisOffsetToPast(10000L);
            initializeClient();
        } catch (Exception e) {
            debug(e.getMessage());
            e.printStackTrace();
        }
        setDebug(datastore.getDebug());
        debug("Initizalized");

        //Set call parameters
        setSiteUrl(datastore.getSiteUrl());
        setStartDate(datastore.getStartDate());
        setEndDate(datastore.getEndDate());
        if(!(dimensions = datastore.getDimensions()).equals("")){
          setDimensions(dimensions);
        }
        if(!(searchType = datastore.getSearchType()).equals("")){
            setSearchType(searchType);
        }
        if((dimensionsFilterGroups = datastore.getDimensionsFilterGroups()) != ""){
            setDimensionsFilterGroups(dimensionsFilterGroups);
        }
        if(!(aggregationType = datastore.getAggregationType()).equals("")){
            setAggregationType(aggregationType);
        }
        if(!datastore.getRowLimit().equals("")){
            rowLimit = Integer.parseInt(datastore.getRowLimit());
            setRowLimit(rowLimit);
        }
        if(!datastore.getStartRow().equals("")){
            startRow = Integer.parseInt(datastore.getStartRow());
            setStartRow(startRow);
        }
        try {
            if (debug) {
                printAllSites();
            }
            fetchData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Producer
    public Record next() {
        // this is the method allowing you to go through the dataset associated
        // to the component configuration
        //
        // return null means the dataset has no more data to go through
        // you can use the builderFactory to create a new Record.
        debug("Record next...");
        try{
            if (iterator < max) {
                debug("Fetch data...");
                iterator++;
                return builderFactory.newRecordBuilder().withString("response", fetchData()).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential conInputSourcenections you created or data you cached
    }


    /* Initialization methods */
    private void initializeClient() throws Exception {
        final Credential credential;
        credential = authorizeWithServiceAccount();
        webmastersService = new Webmasters.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new HttpRequestInitializer() {
                    @Override
                    public void initialize(final HttpRequest httpRequest) throws IOException {
                        credential.initialize(httpRequest);
                        httpRequest.setConnectTimeout(timeoutInSeconds * 1000);
                        httpRequest.setReadTimeout(timeoutInSeconds * 1000);
                    }
                })
                .setApplicationName("tSearchAnalyticsInput")
                .build();
    }
    private Credential authorizeWithServiceAccount() throws Exception {
        debug("Authorize with service account...");
        if (keyFile == null) {
            throw new Exception("KeyFile not set!");
        }
        if (!keyFile.canRead()) {
            throw new IOException("keyFile:" + keyFile.getAbsolutePath()
                    + " is not readable");
        }
        if (accountEmail == null || accountEmail.isEmpty()) {
            throw new Exception("account email cannot be null or empty");
        }
        // Authorization.
        return new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(accountEmail)
                .setServiceAccountScopes(Arrays.asList(WebmastersScopes.WEBMASTERS_READONLY))
                .setServiceAccountPrivateKeyFromP12File(keyFile)
                .setClock(new Clock() {
                    @Override
                    public long currentTimeMillis() {
                        // we must be sure, that we are always in the past from Googles point of view
                        // otherwise we get an "invalid_grant" error
                        return System.currentTimeMillis() - timeMillisOffsetToPast;
                    }
                })
                .build();
    }

    private void printAllSites() throws IOException {
        debug("Print all sites...");
        List<String> allSites = new ArrayList<String>();
        Webmasters.Sites.List request = webmastersService.sites().list();

        // Get all sites that are verified
        debug("Get all sites...");
        SitesListResponse siteList = request.execute();
        for (WmxSite currentSite : siteList.getSiteEntry()) {
            allSites.add(currentSite.getSiteUrl());
        }

        // Print all verified sites
        debug("Print all sites...");
        for (String currentSite : allSites) {
            debug(currentSite);
        }
    }

    private String fetchData() throws IOException {
        debug("Fetch data...");
        request = new SearchAnalyticsQueryRequest();
        request.setStartDate(getStartDate());
        request.setEndDate(getEndDate());
        //List<String> dimensions = new ArrayList<String>();
        //dimensions.add("page");
        //dimensions.add("query");
        //dimensions.add("country");
        //dimensions.add("device");
        //dimensions.add("date");
        //request.setDimensions(dimensions);
        if(dimensionsList != null){
          request.setDimensions(dimensionsList);
        }
        if(searchType != ""){
            request.setSearchType(searchType);
        }
        if(apiDimensionFilterGroups != null){
            request.setDimensionFilterGroups(apiDimensionFilterGroups);
        }
        if(aggregationType != ""){
            request.setAggregationType(aggregationType);
        }
        if(rowLimit != null){
            request.setRowLimit(rowLimit);
        }
        if(startRow != null){
            request.setStartRow(startRow);
        }
        Query query = webmastersService.searchanalytics().query(getSiteUrl(), request);
        debug("Get data...");
        response = query.execute();

        debug("Return data");
        return response.toPrettyString();

    }

    //Setters & getters
    private void setAccountEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        accountEmail = email.trim();
    }
    private void setKeyFile(String file) {
        if (file == null || file.trim().isEmpty()) {
            throw new IllegalArgumentException("Key file path cannot be null or empty");
        }
        keyFile = new File(file.trim());
    }
    private void setSiteUrl(String siteUrl){
        this.siteUrl = siteUrl;
    }
    private String getSiteUrl() {
        return this.siteUrl;
    }
    private void setStartDate(String yyyy_MM_dd) {
        this.startDate = yyyy_MM_dd;
    }
    private String getStartDate() {
        return this.startDate;
    }
    private void setEndDate(String yyyy_MM_dd) {
        this.endDate = yyyy_MM_dd;
    }
    private String getEndDate() {
        return this.endDate;
    }
    private void setDimensions(String dimensions) {
        this.dimensionsList = Arrays.asList(dimensions.split("\\s*,\\s*"));
    }
    private void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    private void setDimensionsFilterGroups(String dimensionsFilterGroups) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<ApiDimensionFilterGroup> objects = mapper.readValue(dimensionsFilterGroups, new TypeReference<List<ApiDimensionFilterGroup>>(){});
            this.apiDimensionFilterGroups = objects;
        } catch (IOException e) {
            debug(e.getStackTrace().toString());
        }
    }
    private void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    private void setRowLimit(int limit){
        this.rowLimit = limit;
    }
    private void setStartRow(int row){
        this.startRow = row;

    }
    private void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    private void setTimeMillisOffsetToPast(long timeMillisOffsetToPast){
         this.timeMillisOffsetToPast = timeMillisOffsetToPast;
    }

    // Debugging & logging
    private void setDebug(boolean status){
        if (logger != null) {
            if (status) {
                logger.setLevel(Level.DEBUG);
            } else {
                logger.setLevel(Level.INFO);
            }
        }
        this.debug = status;
    }
    private void debug(String message) {
        if (debug) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(message);
            } else {
                System.out.println("DEBUG: " + message);
            }
        }
    }


}

