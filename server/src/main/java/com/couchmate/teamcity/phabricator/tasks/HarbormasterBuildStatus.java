package com.couchmate.teamcity.phabricator.tasks;

import com.couchmate.teamcity.phabricator.AppConfig;
import com.couchmate.teamcity.phabricator.HttpRequestBuilder;
import com.couchmate.teamcity.phabricator.StringKeyValue;


import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;

public class HarbormasterBuildStatus extends Task {

    private final Status buildStatus;
    private final BuildLog buildLog;
    private AppConfig appConfig;
    private HttpPost httpPost = null;


    public HarbormasterBuildStatus(final AppConfig appConfig, Status buildStatus, BuildLog buildLog) {
        this.appConfig = appConfig;
        this.buildStatus = buildStatus;
        this.buildLog = buildLog;
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
                builder
                        .addFormParam(new StringKeyValue("type", "fail"))
                        .addFormParam(new StringKeyValue("lint[0][name]", "Build Failure"))
                        .addFormParam(new StringKeyValue("lint[0][code]", "FAIL123"))
                        .addFormParam(new StringKeyValue("lint[0][severity]", "error"))
                        .addFormParam(new StringKeyValue("lint[0][path]", "android/test/path.Java"));
                if (buildLog.getLastMessage() != null) {
                    builder.addFormParam(new StringKeyValue("lint[0][description]", buildLog.getLastMessage().toString()));
                }

            }

            this.httpPost = (HttpPost) builder.build();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void execute() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(this.httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void teardown() {

    }
}


