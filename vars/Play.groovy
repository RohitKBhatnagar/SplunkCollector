

package vars

//import org.company.PostSplunkData
import com.cloudbees.groovy.cps.NonCPS
import hudson.model.Executor

import org.company.collect.LabelBuild
import org.company.collect.PostSplunkData
import org.company.ioc.ContextRegistry

/// Workspace cleaner script to be run as a pipeline job
/// Author: Rohit K. Bhatnagar
/// Modified By: Rohit K. Bhatnagar
/// Modified On: August 02, 2020
/// Modified Reason: Adding support for Label referenced Nodes and Hourly statistics for builds conducted.

import java.text.SimpleDateFormat

import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
//import com.cloudbees.groovy.cps.NonCPS

//class LabelBuild
//{
//    long Count
//    String Name
//    String StartTime
//    String StatusSummary
//    long Duration
//    String HostName
//    String Label
//}

//def call(def jenkinsMasterWorkFlowItems = Jenkins.getInstanceOrNull().getAllItems(WorkflowJob), def jenkinsMasterNodes = Jenkins.getInstanceOrNull().getNodes(), def String strMaster = java.net.InetAddress.getLocalHost())
def call(def jenkinsMasterWorkFlowItems = Jenkins.getInstanceOrNull().getAllItems(), def jenkinsMasterNodes = Jenkins.getInstanceOrNull().getNodes(), def String strMaster = java.net.InetAddress.getLocalHost())
{
    def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
    def startDate = new Date()
    println "Start time : ${sdf.format(startDate)}"

    def slave_label_map = [:]
    def iCount = 0
    for (slave in jenkinsMasterNodes)
    {
        def words = slave.labelString.split()
        def labelListForSlave = []
        words.each() {
            labelListForSlave.add(it);
        }
        slave_label_map.put(slave.name, labelListForSlave)
    }

    Date before60Minutes = getBefore60Minutes()

    ArrayList<LabelBuild> lstLblBlds = new ArrayList<LabelBuild>();
    def allItems = jenkinsMasterWorkFlowItems
    println "Total Workflow Items - ${allItems.size()}"
    try
    {
        def myItems = allItems.findAll
        {
            try
            {
                WorkflowRun build = it.builds.first()
                new Date(build.startTimeInMillis).after(before60Minutes)
            }
            catch(Exception exp)
            {
                //Do Nothing
                //println("${it.class.simpleName} - ${exp.message}")
            }
        }.collect
        {
            it.builds.findAll
                    {
                        WorkflowRun build ->
                            new Date(build.startTimeInMillis).after(before60Minutes)
                    }
        }.flatten().findAll
        {
            WorkflowRun build ->
                iCount++
                try {
                    def sHostName = ""
                    String sLabel = ""

                    /******************************************************************************************
                     * We don't need to read the log to pull out Label and HostName details *******************
                     ByteArrayOutputStream os = new ByteArrayOutputStream()
                     InputStreamReader is = build.logText.readAll()
                     PlainTextConsoleOutputStream pos = new PlainTextConsoleOutputStream(os)
                     IOUtils.copy(is, pos)
                     String text = os.toString()
                     println "${iCount} :: Build URL - ${build.getUrl()}"
                     if (text.contains("Running on")) {
                     def srchTxt = /jnk.* / //Please remove space in between * & / to reactivate log reading
                     def findNode = (text =~ /$srchTxt/)
                     String sHostString = findNode[0]//.toString()
                     int iIndex = sHostString.indexOf('in', 0)
                     if (iIndex > 0) {
                     sHostName = sHostString.substring(0, iIndex - 1)
                     sLabel = slave_label_map.find { it.key == sHostName }?.value
                     }
                     }
                     os.close()
                     is.close()
                     pos.close()
                     ******************************************************************************************/
                    //SCMs
                    Map lstSCMs = [:]
                    def scmsLst = build.getSCMs();
                    scmsLst.each {
                        lstSCMs.put('SCMKey', it.getKey()); // - Type - ${it.getType()}}"
                    }
                    //Build based ChangeSets Listing
                    ArrayList<Map> lstChgSetBlds = new ArrayList<Map>();//https://javadoc.jenkins.io/plugin/workflow-job/org/jenkinsci/plugins/workflow/job/WorkflowRun.html
                    List<Map> lstOfChgSets = new ArrayList<Map>();
                    def lstChangeSets = build.getChangeSets();  //https://javadoc.jenkins.io/plugin/git/hudson/plugins/git/GitChangeSetList.html
                    lstChangeSets.eachWithIndex { it, itCount ->
                        Map lstChgSets = [:];
                        lstChgSets.put('Kind', it.getKind())
                        lstChgSets.put('EmptySet', it.isEmptySet())
                        def lstGitChgSet = it.getLogs();
                        List<Map> lstOfGitChangeSet = new ArrayList<Map>();
                        lstGitChgSet.eachWithIndex { itSub, iChgCount ->
                            Map lstGitChangeSet = [:]
                            lstGitChangeSet.put('AuthorMail', itSub.getAuthorEmail())
                            lstGitChangeSet.put('AuthorName', itSub.getAuthorName())
                            lstGitChangeSet.put('Branch', itSub.getBranch())
                            lstGitChangeSet.put('Comment', itSub.getComment())
                            lstGitChangeSet.put('CommitID', itSub.getCommitId())
                            lstGitChangeSet.put('Date', itSub.getDate())
                            lstGitChangeSet.put('ID', itSub.getId())
                            lstGitChangeSet.put('ParentCommit', itSub.getParentCommit())
                            lstGitChangeSet.put('Revision', itSub.getRevision())
                            lstGitChangeSet.put('TimeStamp', itSub.getTimestamp())

                            List<Map> lstOfFiles = new ArrayList<Map>();
                            def lstAffectedFiles = itSub.getAffectedFiles();
                            lstAffectedFiles.eachWithIndex { itASub, itACount ->
                                Map lstFiles = [:]
                                lstFiles.put('Destination', itASub.getDst())
                                lstFiles.put('Path', itASub.getPath())
                                lstFiles.put('Source', itASub.getSrc())

                                //lstOfFiles.add(itACount, lstFiles)
                                lstOfFiles.add(lstFiles)
                            }
                            //lstOfGitChangeSet.add(iChgCount, lstGitChangeSet);
                            lstOfGitChangeSet.add(lstGitChangeSet);
                            //lstOfGitChangeSet.addAll(iChgCount, lstOfFiles);
                            lstOfGitChangeSet.addAll(lstOfFiles);
                        }
                        //lstOfChgSets.add(itCount, lstChgSets)
                        lstOfChgSets.add(lstChgSets)
                        //lstOfChgSets.addAll(itCount, lstOfGitChangeSet)
                        lstOfChgSets.addAll(lstOfGitChangeSet)
                    }
                    lstChgSetBlds.addAll(lstOfChgSets);

                    //DEBUGGING------
                    //println "Change-Sets - ${lstChgSetBlds}"

                    //--------------------- Build AllActions - START ----------------
                    //ArrayList<Map> lstBldAllActions = new ArrayList<Map>();//https://javadoc.jenkins.io/plugin/workflow-job/org/jenkinsci/plugins/workflow/job/WorkflowRun.html
                    List<Map> lstOfAllBldActions = new ArrayList<Map>();
                    def lstAllActions = build.getAllActions(); //https://javadoc.jenkins-ci.org/hudson/model/Action.html
                    lstAllActions.each {
                        List<Map> lstOfBldActions = new ArrayList<Map>();
                        if(it instanceof org.jenkinsci.plugins.workflow.support.actions.WorkspaceRunAction)
                        {
                            Map lstWSRA = [:];
                            lstWSRA.put("WSRA-DisplayName", it.getDisplayName())
                            lstWSRA.put("WSRA-URL", it.getUrlName())
                            lstWSRA.put("WSRA-IconFileName", it.getIconFileName())

                            //https://javadoc.jenkins.io/plugin/workflow-support/org/jenkinsci/plugins/workflow/support/actions/WorkspaceRunAction.html
                            def lstWkspcActions = it.getActions();

                            List<Map> lstOfWSAI = new ArrayList<Map>();
                            //https://javadoc.jenkins.io/plugin/workflow-support/org/jenkinsci/plugins/workflow/support/actions/WorkspaceActionImpl.html
                            lstWkspcActions.each { itSub ->
                                Map lstWSAI = [:]
                                lstWSAI.put("WSAI-DisplayName", itSub.getDisplayName());

                                sHostName = itSub.getNode()
                                sLabel = itSub.getLabels();
                                //sLabel = sLabel.replace('[', '');
                                //sLabel = sLabel.replace(']', '');
                                lstWSAI.put("WSAI-Labels", sLabel);
                                lstWSAI.put("WSAI-Node", sHostName);
                                lstWSAI.put("WSAI-Parent-Name", itSub.getParent().getDisplayName());
                                lstWSAI.put("WSAI-Parent-ID", itSub.getParent().getId());
                                lstWSAI.put("WSAI-Parent-URL", itSub.getParent().getUrl());
                                lstWSAI.put("WSAI-Parent-Active", itSub.getParent().isActive());
                                lstWSAI.put("WSAI-Parent-FunctionName", itSub.getParent().getDisplayFunctionName());
                                lstWSAI.put("WSAI-Parent-TypeDisplayName", itSub.getParent().getTypeDisplayName()); //Gets a human readable name for this type of the node.
                                lstWSAI.put("WSAI-Parent-TypeFunctionName", itSub.getParent().getTypeFunctionName());
                                lstWSAI.put("WSAI-Path", itSub.getPath());
                                lstWSAI.put("WSAI-URLName", itSub.getUrlName());

                                //DEBUGGING------
                                //println "DEBUGGING-WSAI - ${lstWSAI}";

                                lstOfWSAI.add(lstWSAI);
                            }

                            lstOfBldActions.add(lstWSRA);
                            lstOfBldActions.addAll(lstOfWSAI);
//                            lstOfAllBldActions.add(lstWSRA);
//                            lstOfAllBldActions.addAll(lstOfWSAI);

                            //lstBldAllActions.add(lstWSRA);
                            //lstBldAllActions.addAll(lstOfWSAI);
                        }
                        lstOfAllBldActions.addAll(lstOfBldActions);

                        /**/
                        if(it instanceof org.jenkinsci.plugins.pipeline.modeldefinition.actions.ExecutionModelAction) {
                            Map lstEMA = [:]
                            lstEMA.put("EMA-UUID", it.getStagesUUID());
                            lstOfBldActions.add(lstEMA);
                            lstOfBldActions.addAll(it.getStagesList());
                            lstOfBldActions.addAll(it.getPipelineDefs());
                        }
                        /**
                         Map lstEMA = [:]
                         //https://javadoc.jenkins.io/plugin/pipeline-model-definition/org/jenkinsci/plugins/pipeline/modeldefinition/actions/ExecutionModelAction.html
                         lstEMA.put("EMA-UUID", it.getStagesUUID());
                         List<Map> lstOfMASTStages = new ArrayList<Map>();
                         def lstStages = it.getStagesList();
                         //https://javadoc.jenkins.io/plugin/pipeline-model-api/org/jenkinsci/plugins/pipeline/modeldefinition/ast/ModelASTStages.html
                         lstStages.eachWithIndex { itMSub, iMSubCount ->
                         //Map lstMASTStagesList = [:];
                         List<Map> lstOfMASTStagesList = new ArrayList<Map>();
                         def lstStage = itMSub.getStages()
                         //https://javadoc.jenkins.io/plugin/pipeline-model-api/org/jenkinsci/plugins/pipeline/modeldefinition/ast/ModelASTStage.html
                         lstStage.eachWithIndex { itSub, iSubCount ->
                         Map lstMASTStagesList = [:];
                         //lstMASTStagesList.put("MAST-Stages-Branches", itSub.getBranches())
                         lstMASTStagesList.put("MAST-Stages-FailFast", itSub.getFailFast())
                         lstMASTStagesList.put("MAST-Stages-Matrix", itSub.getMatrix())
                         lstMASTStagesList.put("MAST-Stages-Name", itSub.getName())
                         lstMASTStagesList.put("MAST-Stages-Parallel", itSub.getParallel())
                         lstMASTStagesList.put("MAST-Stages", itSub.getStages());

                         lstOfMASTStagesList.add(lstMASTStagesList);
                         }

                         lstOfMASTStages.addAll(lstOfMASTStagesList);
                         }

                         List<Map> lstOfMASTPipeDefs = new ArrayList<Map>();
                         def lstPipeDefs = it.getPipelineDefs();
                         //https://javadoc.jenkins.io/plugin/pipeline-model-api/org/jenkinsci/plugins/pipeline/modeldefinition/ast/ModelASTPipelineDef.html
                         lstPipeDefs.eachWithIndex { itSub, iSubCount ->
                         Map lstMASTPipeDefs = [:];
                         //lstMASTPipeDefs.put("MAST-Pipes-Agent", itSub.getAgent());
                         //lstMASTPipeDefs.put("MAST-Pipes-Environment", itSub.getEnvironment());
                         lstMASTPipeDefs.put("MAST-Pipes-Libraries", itSub.getLibraries());
                         lstMASTPipeDefs.put("MAST-Pipes-Options", itSub.getOptions());
                         lstMASTPipeDefs.put("MAST-Pipes-Parameters", itSub.getParameters());
                         //lstMASTPipeDefs.put("MAST-Pipes-PostBuild", itSub.getPostBuild());
                         //lstMASTPipeDefs.put("MAST-Pipes-Stages", itSub.getStages());
                         //lstMASTPipeDefs.put("MAST-Pipes-Tools", itSub.getTools());
                         lstMASTPipeDefs.put("MAST-Pipes-Triggers", itSub.getTriggers());

                         lstOfMASTPipeDefs.add(lstMASTPipeDefs);
                         }

                         lstOfBldActions.add(lstEMA);
                         lstOfBldActions.addAll(lstOfMASTStages);
                         lstOfBldActions.addAll(lstOfMASTPipeDefs);
                         }
                         **/
                        if(it instanceof jenkins.metrics.impl.TimeInQueueAction)
                        {
                            Map lstTQA = [:];
                            //https://javadoc.jenkins.io/plugin/metrics/jenkins/metrics/impl/TimeInQueueAction.html
                            lstTQA.put("BlockedDurationMillis", it.getBlockedDurationMillis());
                            lstTQA.put("BlockedDurationString", it.getBlockedDurationString());
                            lstTQA.put("BlockedTimeMillis", it.getBlockedTimeMillis());
                            lstTQA.put("BlockedTimeString", it.getBlockedTimeString());
                            lstTQA.put("BuildableDurationMillis", it.getBuildableDurationMillis());
                            lstTQA.put("BuildableDurationString", it.getBuildableDurationString());
                            lstTQA.put("BuildableTimeMillis", it.getBuildableTimeMillis());
                            lstTQA.put("BuildableTimeString", it.getBuildableTimeString());
                            lstTQA.put("BuildingDurationMillis", it.getBuildingDurationMillis());
                            lstTQA.put("BuildingDurationString", it.getBuildingDurationString());
                            lstTQA.put("DisplayName", it.getDisplayName());
                            lstTQA.put("ExecutingTimeMillis", it.getExecutingTimeMillis());
                            lstTQA.put("ExecutingTimeString", it.getExecutingTimeString());
                            lstTQA.put("ExecutorUtilization", it.getExecutorUtilization());
                            lstTQA.put("IconFileName", it.getIconFileName());
                            lstTQA.put("QueuingDurationMillis", it.getQueuingDurationMillis());
                            lstTQA.put("QueuingDurationString", it.getQueuingDurationString());
                            lstTQA.put("QueuingTimeMillis", it.getQueuingTimeMillis());
                            lstTQA.put("QueuingTimeString", it.getQueuingTimeString());
                            //https://javadoc.jenkins.io/hudson/model/Run.html?is-external=true
                            lstTQA.put("Run-DisplayName", it.getRun().getDisplayName());
                            lstTQA.put("Run-FullDisplayName", it.getRun().getFullDisplayName());
                            lstTQA.put("Run-BuildStatusURL", it.getRun().getBuildStatusUrl());
                            lstTQA.put("Run-Duration", it.getRun().getDuration());
                            lstTQA.put("Run-DurationString", it.getRun().getDurationString());

                            lstTQA.put("SubTaskCount", it.getSubTaskCount());
                            lstTQA.put("TotalDurationMillis", it.getTotalDurationMillis());
                            lstTQA.put("TotalDurationString", it.getTotalDurationString());
                            lstTQA.put("UrlName", it.getUrlName());
                            lstTQA.put("WaitingDurationMillis", it.getWaitingDurationMillis());
                            lstTQA.put("WaitingDurationString", it.getWaitingDurationString());
                            lstTQA.put("WaitingTimeMillis", it.getWaitingTimeMillis());
                            lstTQA.put("WaitingTimeString", it.getWaitingTimeString());
                            lstTQA.put("HasSubTasks", it.isHasSubTasks());

                            //DEBUGGING------
                            //println "DEBUGGING-TimeInQueueAction - ${lstTQA}";

                            lstOfBldActions.add(lstTQA);
                        }
                        if(it instanceof hudson.plugins.git.util.BuildData)
                        {
                            Map lstBD = [:];
                            //https://javadoc.jenkins.io/plugin/git/hudson/plugins/git/util/BuildData.html
                            //lstBD.put("BuildsByBranchName", it.getBuildsByBranchName().getDisplayName());
                            //lstBD.put("BuildsByBranchName-SCM", it.getBuildsByBranchName().getScmName());
                            //lstBD.put("BuildsByBranchName-URL", it.getBuildsByBranchName().getUrlName());
                            //lstBD.put("BuildsByBranchName-Index", it.getBuildsByBranchName().getIndex());
                            lstBD.put("BD-DisplayName", it.getDisplayName());
                            lstBD.put("BD-Index", it.getIndex());
                            lstBD.put("BD-SCMName", it.getScmName());
                            lstBD.put("BD-URLName", it.getUrlName());

                            //DEBUGGING------
                            //println "DEBUGGING-BuildData - ${lstBD}";

                            lstOfBldActions.add(lstBD);
                            lstOfBldActions.addAll(it.getBuildsByBranchName());
                        }
                        if(it instanceof jenkins.metrics.impl.SubTaskTimeInQueueAction)
                        {
                            Map lstSTTQA = [:];
                            lstSTTQA.put("BlockedDurationMillis", it.getBlockedDurationMillis());  //Returns the duration this SubTask spent in the queue because it was blocked.
                            lstSTTQA.put("BuildableDurationMillis", it.getBuildableDurationMillis());  //Returns the duration this SubTask spent in the queue in a buildable state.
                            lstSTTQA.put("ExecutingDurationMillis", it.getExecutingDurationMillis());//Returns the duration this SubTask spent executing.
                            lstSTTQA.put("DisplayName", it.getDisplayName());
                            lstSTTQA.put("IconFileName", it.getIconFileName());
                            lstSTTQA.put("QueuingDurationMillis", it.getQueuingDurationMillis()); //How long spent queuing (this is the time from when the WorkUnitContext.item entered the queue until WorkUnitContext.synchronizeStart() was called.
                            lstSTTQA.put("UrlName", it.getUrlName());
                            lstSTTQA.put("WaitingDurationMillis", it.getWaitingDurationMillis()); //Returns the duration this SubTask spent in the queue waiting before it could be considered for execution.
                            lstSTTQA.put("WorkUnitCount", it.getWorkUnitCount()); //Returns the number of executor slots occupied by this SubTask.

                            //DEBUGGING------
                            //println "DEBUGGING-SubTaskTimeInQueueAction - ${lstSTTQA}";

                            lstOfBldActions.add(lstSTTQA);
                        }

                        lstOfAllBldActions.addAll(lstOfBldActions);

                        //lstBldAllActions.addAll(lstOfBldActions);
                    }
                    //lstBldAllActions.addAll(lstOfAllBldActions);
                    //lstBldAllActions.addAll(lstOfAllBldActions);
                    //--------------------- Build AllActions - END ------------------

                    //DEBUGGING------
                    //println "Build-AllActions - ${lstOfAllBldActions}"; // ${lstBldAllActions}"

                    //https://javadoc.jenkins-ci.org/hudson/model/Run.html - //https://javadoc.jenkins-ci.org/hudson/model/Executor.html
                    Executor buildExecutor = build.getExecutor();
                    long queueWaitTime = -1; boolean executorStuck = false; boolean executorParked = false;
                    if(buildExecutor) {
                        queueWaitTime = buildExecutor.getTimeSpentInQueue();
                        executorStuck = buildExecutor.isLikelyStuck()
                        executorParked = buildExecutor.isParking()
                    }

                    //Add all details to the List<LabelBuild>
                    def myLabelBuild = new LabelBuild(Count: iCount, Name: build, StartTime: build.time, StatusSummary: build.getBuildStatusSummary().message, HostName: sHostName, Label: sLabel, BuildID: build.getId(), Duration: build.getDuration(), DurationString: build.getDurationString(), Environment: build.getEnvironment(), EstimatedDuration: build.getEstimatedDuration(), TimeSpentInQueue: queueWaitTime, BuildStuck: executorStuck, BuildParked: executorParked, ExternalizableId: build.getExternalizableId(), FullDisplayName: build.getFullDisplayName(), HasArtifacts: build.getHasArtifacts(), Number: build.getNumber(), Parent: build.getParent().getDisplayName(), QueueId: build.getQueueId(), Result: build.getResult().toString(), RootDir: build.getRootDir().getAbsolutePath(), SearchUrl: build.getSearchUrl(), StartTimeInMillis: build.getStartTimeInMillis(), Time: build.getTime(), TimeInMillis: build.getTimeInMillis(), TimestampString: build.getTimestampString(), TimestampString2: build.getTimestampString2(), Url: build.getUrl(), SCMs: lstSCMs, ChangeSets: lstChgSetBlds, CulpritIDs: build.getCulpritIds(), Culprits: build.getCulprits(), LogFile: build.getLogFile().getPath(), LogLength: build.getLogFile().length(), AllowKill: build.hasAllowKill(), AllowTerm: build.hasAllowTerm(), StartedYet: build.hasntStartedYet(), IsBuilding: build.isBuilding(), InProgress: build.isInProgress(), LogUpdated: build.isLogUpdated(), BuildAllActions: lstOfAllBldActions); //lstBldAllActions);

                    lstLblBlds.add(myLabelBuild);

                    //DEBUGGING------
                    //println "LabelBuild - ${lstLblBlds}";
                }
                catch(Exception e)
                {
                    println "Exception raised... - ${e.message}";
                }
        }*.absoluteUrl.join('\n')

        println "Build count after - ${before60Minutes} are - ${iCount - 1}"

        //String sJsonRoot = "LabelStats";
        //------------------------------------------------------------------------------------------
        /////////////////////////////////////////////////////////////////////
        //Following creates just one JSON with all elements collected in the ArrayList ////////
        //============================================================================ ////////
//        def jsonLabels = new groovy.json.JsonBuilder()
//        jsonLabels (
//                lstLblBlds.collect {
//                    [Count: it.Count, Name: it.Name, StartTime:it.StartTime, StatusSummary: it.StatusSummary, Duration:it.Duration, HostName: it.HostName, Label:it.Label, Master: strMaster]
//                }
//        )
        /////////////////////////////////////////////////////////////////////
//        println "${jsonLabels.toPrettyString()}"
//        //println "Build count after - ${before60Minutes} are - ${iCount - 1}"
//        def oPostJson = new PostSplunkData(jsonLabels, this);
//        ContextRegistry.registerDefaultContext(this);
//        def strOut = oPostJson.PostSplunkMessages();
//        println "${strOut}"

        //------------------------------------------------------------------------------------------
        /////////////////////////////////////////////////////////////////////////////////////
        ////Following is an attempt to create multiple JSON's from the ArrayList elements ///
        ////============================================================================= ///
//        lstLblBlds.each {it->
//            def lblCount = it.Count
//            def lblName = "\"" + it.Name + "\""
//            def lblStartTime = "\"" + it.StartTime + "\""
//            def lblStatusSummary = "\"" + it.StatusSummary + "\""
//            def lblDuration = it.Duration
//            def lblHostName = "\"" + it.HostName + "\""
//            def lblLabel = "\"" + it.Label + "\""
//            def lblMaster = "\"" + strMaster + "\""
//            println "${lblCount} - ${lblName} - ${lblStartTime} - ${lblStatusSummary} - ${lblDuration} - ${lblHostName} - ${lblLabel} - ${lblMaster}";
//                def jsonLabels = new groovy.json.JsonBuilder();
//
//                jsonLabels { "LabelStats" ({
//                        "Count" lblCount
//                        "Name" lblName
//                        "StartTime" lblStartTime
//                        "StatusSummary" lblStatusSummary
//                        "Duration" lblDuration
//                        "HostName" lblHostName
//                        "Label" lblLabel
//                        "Master" lblMaster
//                    })
//                }
//
//                println "${jsonLabels.toPrettyString()}"
//
//                def oPostJson = new PostSplunkData(jsonLabels, this);
//
//                ContextRegistry.registerDefaultContext(this);
//
//                def strOut = oPostJson.PostSplunkMessages();
//                println "${strOut}"
//        }
        /////////////////////////////////////////////////////////////////////////////////////


        /------------------------------------------------------------------------------------------
        /////////////////////////////////////////////////////////////////////////////////////
        ////Following is an attempt to entire list of ArrayList elements to be parsed as JSON///
        ////============================================================================= ///
        def oPostJson = new PostSplunkData(lstLblBlds, this);
        ContextRegistry.registerDefaultContext(this);
        def strOut = oPostJson.PostMapToSplunk(strMaster);
        println "${strOut}"
    }
    catch(StringIndexOutOfBoundsException sioobe)
    {
        println "STRING INDEX OUT OF BOUNDS EXCEPTION - ${sioobe.message}"
    }
    catch(Exception exp)
    {
        println "EXCEPTION - ${exp.message}"
    }

//--------------------------------------------------
    def endDate = new Date()
    println "End time: ${sdf.format(endDate)}"

    return
}

/***/

@NonCPS
def getBefore60Minutes ()
{
    use( groovy.time.TimeCategory ) {
        return new Date() - 60.minutes; //60.minutes.ago
    }
}

@NonCPS
void PrintExecutionTime(Date dtStart, Date dtEnd)
{
    use(groovy.time.TimeCategory) {
        def duration = dtEnd - dtStart
        print "Elapsed time: Days: ${duration.days}, Hours: ${duration.hours}, Minutes: ${duration.minutes}, Seconds: ${duration.seconds}"
    }
}
