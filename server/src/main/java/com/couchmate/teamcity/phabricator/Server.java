package com.couchmate.teamcity.phabricator;

import com.couchmate.teamcity.phabricator.tasks.HarbormasterBuildStatus;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

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
        logger.info("Phab Server Initialized");
    }

    @Override
    public void buildStarted(@NotNull SRunningBuild runningBuild) {
        super.buildStarted(runningBuild);
        logger.info("Build Started");
        new Thread(new BuildTracker(runningBuild)).start();
    }

    @Override
    public void buildFinished(@NotNull SRunningBuild build) {
        logger.info("Build Finished");
        buildInterruptedOrFinished(build);
    }

    @Override
    public void buildInterrupted(@NotNull SRunningBuild build) {
        logger.info("Build Interrupted");
        buildInterruptedOrFinished(build);
    }

    private void buildInterruptedOrFinished(@NotNull SRunningBuild build) {
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
        new HarbormasterBuildStatus(appConfig, build.getBuildStatus(), build.getBuildId(), build.getBuildTypeId(), logger).run();
    }
}
