package com.couchmate.teamcity.phabricator;

import com.couchmate.teamcity.phabricator.tasks.HarbormasterBuildStatus;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.tests.TestInfo;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends BuildServerAdapter {

    public static final String BUILD_FEATURE_PHABRICATOR = "phabricator";
    private Map<String, List<STestRun>> tests = new HashMap<>();

    private PhabLogger logger;

    public Server(
            @NotNull final EventDispatcher<BuildServerListener> buildServerListener,
            @NotNull final PhabLogger logger
    ) {
        buildServerListener.addListener(this);
        this.logger = logger;
        Loggers.SERVER.info("Phab Server Initialized");
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild runningBuild) {
        super.buildStarted(runningBuild);
        new Thread(new BuildTracker(runningBuild)).start();
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        super.buildFinished(build);
        BuildPromotion bp = build.getBuildPromotion();

        Map<String, String> configs = new HashMap<>();

        configs.putAll(bp.getBuildParameters());

        for (SBuildFeatureDescriptor buildFeature : build.getBuildFeaturesOfType(BUILD_FEATURE_PHABRICATOR)) {
            configs.putAll(buildFeature.getParameters());
        }
        AppConfig appConfig = new AppConfig();

        appConfig.setParams(configs);
        appConfig.setLogger(this.logger);
        appConfig.parse();

        bp.getBuildLog();
        build.getBuildStatus();

        new HarbormasterBuildStatus(appConfig, build.getBuildStatus(), bp.getBuildLog());
    }
}
