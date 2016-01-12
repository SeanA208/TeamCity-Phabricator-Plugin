package com.couchmate.teamcity.phabricator.tasks;

import com.couchmate.teamcity.phabricator.AppConfig;
import com.couchmate.teamcity.phabricator.HttpRequestBuilder;
import com.couchmate.teamcity.phabricator.PhabLogger;
import com.couchmate.teamcity.phabricator.StringKeyValue;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;

public class HarbormasterBuildStatus extends Task {

    private final Status buildStatus;
    private final PhabLogger logger;
    private final long buildId;
    private final String buildTypeId;
    private AppConfig appConfig;
    private HttpPost httpPost = null;

    public HarbormasterBuildStatus(AppConfig appConfig, Status buildStatus, long buildId, String buildTypeId, PhabLogger logger) {
        this.appConfig = appConfig;
        this.buildStatus = buildStatus;
        this.buildId = buildId;
        this.buildTypeId = buildTypeId;
        this.logger = logger;
    }


    @Override
    protected void setup() {
        try {
            HttpRequestBuilder builder = new HttpRequestBuilder()
                    .post()
                    .setUrl(this.appConfig.getPhabricatorUrl())
                    .setPath("/api/harbormaster.sendmessage")
                    .addFormParam(new StringKeyValue("api.token", this.appConfig.getConduitToken()))
                    .addFormParam(new StringKeyValue("buildTargetPHID", this.appConfig.getHarbormasterTargetPHID()));

            if (buildStatus.isSuccessful()) {
                builder.addFormParam(new StringKeyValue("type", "pass"));
            } else {
//                https://teamcity.d.musta.ch/viewLog.html?buildId=328&buildTypeId=Android_PhabricatorStagingBuilder&tab=buildResultsDiv
                String buildLogPath = "https://teamcity.d.musta.ch/viewLog.html?";
                buildLogPath += "buildId=" + buildId;
                buildLogPath += "&buildTypeId=" + buildTypeId;
                buildLogPath += "&tab=buildResultsDiv";

                builder
                        .addFormParam(new StringKeyValue("type", "fail"))
                        .addFormParam(new StringKeyValue("lint[0][severity]", "error"))
                        .addFormParam(new StringKeyValue("lint[0][path]", ""))
                        .addFormParam(new StringKeyValue("lint[0][code]", "Build Failure"))
                        .addFormParam(new StringKeyValue("lint[0][name]", buildLogPath));


            }
            logger.info(builder.toString());
            this.httpPost = (HttpPost) builder.build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(this.httpPost);
            logger.info("HarbormasterBuildStatus executed");
        } catch (Exception e) {
            logger.info("HarbormasterBuildStatus failed with " + e.toString());
        }
    }

    @Override
    protected void teardown() {

    }
}


