package com.couchmate.teamcity.phabricator;


import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Agent extends AgentLifeCycleAdapter {

    private BuildProgressLogger buildLogger = null;
    private PhabLogger logger = null;
    private AppConfig appConfig = null;
    private Collection<AgentBuildFeature> buildFeatures = null;

    public Agent(
            @NotNull final EventDispatcher<AgentLifeCycleListener> eventDispatcher,
            @NotNull final PhabLogger phabLogger,
            @NotNull final AppConfig appConfig
    ) {
        eventDispatcher.addListener(this);
        this.logger = phabLogger;
        this.appConfig = appConfig;
        this.logger.info("Instantiated");
    }

    @Override
    public void buildStarted(@NotNull AgentRunningBuild runningBuild) {
        //super.buildStarted(runningBuild);
        //Get logger
        this.logger.setBuildLogger(runningBuild.getBuildLogger());
        this.logger.info("Started");
        this.buildFeatures = runningBuild.getBuildFeaturesOfType("phabricator");
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        //super.beforeRunnerStart(runner);
        //If plugin enabled, run it
        try {
            Map<String, String> configs = new HashMap<>();
            configs.putAll(runner.getBuildParameters().getEnvironmentVariables());
            configs.putAll(runner.getConfigParameters());
            if (!this.buildFeatures.isEmpty()) {
                configs.putAll(this.buildFeatures.iterator().next().getParameters());
            } else {
                logger.info("No build features found");
            }
            this.appConfig.setParams(configs);
            this.appConfig.setLogger(this.logger);
            this.appConfig.parse();

        } catch (Exception e) {
            this.logger.warn("BeforeRunnerStartError", e);
        }

    }

}
