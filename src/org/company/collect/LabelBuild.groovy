package org.company.collect

import hudson.model.BallColor
import hudson.model.Executor

class LabelBuild {
    long Count
    String Name
    String StartTime
    String StatusSummary
    long Duration
    String HostName
    String Label

    //Following get methods are from https://javadoc.jenkins-ci.org/hudson/model/Run.html

    String BuildID;
    String DurationString
    Map Environment
    //Map EnvVars
    long EstimatedDuration

    //https://javadoc.jenkins-ci.org/hudson/model/Executor.html
    Executor Executor
    long TimeSpentInQueue //Returns the number of milli-seconds the currently executing job spent in the queue waiting for an available executor.
    boolean BuildStuck; //true if the current build is likely stuck
    boolean BuildParked; //true if executor is waiting for a task to execute.

    String ExternalizableId
    String FullDisplayName
    Boolean HasArtifacts
    //BallColor IconColor

    String Number
    //String OneOffExecutor
    String Parent //JobT 	getParent() -     The project this build is for. Calling getDisplayName() to return with a parent name!

    /*String PreviousBuildInProgress
    String PreviousBuildsOverThreshold
    String PreviousBuiltBuild
    String PreviousCompletedBuild
    String PreviousFailedBuild
    String PreviousNotFailedBuild
    String PreviousSuccessfulBuild*/

    long QueueId
    String Result
    String RootDir
    String SearchUrl
    long StartTimeInMillis
    //Object Target
    Date Time
    long TimeInMillis
    //Calendar Timestamp
    String TimestampString
    String TimestampString2
    //String TransientActions
    //String TruncatedDescription
    String Url

    Map SCMs;
    //https://javadoc.jenkins.io/plugin/workflow-job/org/jenkinsci/plugins/workflow/job/WorkflowRun.html#getChangeSets--
    ArrayList<Map> ChangeSets;     //https://javadoc.jenkins.io/plugin/git/hudson/plugins/git/GitChangeSet.html

    String CulpritIDs;
    String Culprits;
    String LogFile
    Long LogLength

    boolean AllowKill;
    boolean AllowTerm;
    boolean StartedYet;
    boolean IsBuilding;
    boolean InProgress;
    boolean LogUpdated;

    //ArrayList<Map> BuildAllActions;
    List<Map> BuildAllActions;
}
