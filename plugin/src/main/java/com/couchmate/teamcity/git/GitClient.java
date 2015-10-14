package com.couchmate.teamcity.git;

import com.couchmate.teamcity.TCPhabException;
import com.couchmate.teamcity.utils.CommandBuilder;

/**
 * Created by mjo20 on 10/12/2015.
 */
public final class GitClient {

    private final String GIT_COMMAND = "git";
    private final String workingDir;

    private GitClient(){
        this.workingDir = null;
    }

    public GitClient(final String workingDir){
        this.workingDir = workingDir;
    }

    public CommandBuilder.Command reset(){
        try{
            return new CommandBuilder()
                    .setCommand(this.GIT_COMMAND)
                    .setAction("reset")
                    .setFlag("--hard")
                    .build();
        } catch (TCPhabException e) { return null; }
    }

    public CommandBuilder.Command clean(){
        try{
            return new CommandBuilder()
                    .setCommand(this.GIT_COMMAND)
                    .setAction("clean")
                    .setArg("-fd")
                    .setArg("-f")
                    .build();
        } catch (TCPhabException e) { return null; }
    }

}
