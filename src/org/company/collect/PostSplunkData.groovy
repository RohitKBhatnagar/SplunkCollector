package org.company.collect

import groovy.json.JsonOutput
import org.company.IStepExecutor
import org.company.ioc.ContextRegistry
import java.util.Date
import java.text.SimpleDateFormat

class PostSplunkData implements Serializable {
    private def jsonData
    private def _currCtxt

    PostSplunkData(def passedData, def currContext)
    {
        this.jsonData = passedData;
        this._currCtxt = currContext;
    }

    def PostSplunkMessages()
    {
        return executeCurlCommand(jsonData);
    }

    def executeCurlCommand(def jsonLabels)
    {
        def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor();
        try {
            //def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/event";
            def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/raw";
            def strToken = "a0ec61b4-cb16-4a62-a159-9c3982d11153"; //Token used by Jenkins App
            //def strToken = "2cbfdb8a-bcff-4194-9ccc-784da4b6c7ad"; //Token used by BTE App


            def outputStream = new StringBuffer();

            steps.node("PROD-LIN7") {

                try {
                    int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data \'${jsonLabels}\'")
                    println "Return code - ${returnStatus}"
                } catch (e) {
                    steps.echo "Exception raised when using CURL command - ${e.message}"
                }
            }

            return outputStream.toString();
        }
        catch(StringIndexOutOfBoundsException sioobexp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - StringIndexOutOfBoundsException raised - ${sioobexp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("StringIndexOutOfBoundsException - \"${strExp}\"")
            }
            return strExp;
        }
        catch (Exception exp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - EXCEPTION raised - ${exp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("Exception - \"${strExp}\"")
            }
            return strExp;
        }
    }

    def PostMapToSplunk(def sMaster)
    {
        def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor();
        try {
            //def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/event";
            def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/raw";
            def strToken = "a0ec61b4-cb16-4a62-a159-9c3982d11153"; //Token used by Jenkins App
            //def strToken = "2cbfdb8a-bcff-4194-9ccc-784da4b6c7ad"; //Token used by BTE App


            def outputStream = new StringBuffer();

            steps.node("PROD-LIN7") {
                jsonData.each { e->
                    LabelBuild oLblBld = e
                    //steps.echo("${oLblBld.Count} - ${oLblBld.Name} - ${oLblBld.StartTime} - ${oLblBld.StatusSummary} - ${oLblBld.HostName} - ${oLblBld.Label} - ${oLblBld.Duration} - ${sMaster}")

                    Map splunkJSON = [Count:oLblBld.Count, Name:oLblBld.Name, StartTime:oLblBld.StartTime, StatusSummary:oLblBld.StatusSummary, Duration:oLblBld.Duration, HostName:oLblBld.HostName, Label:oLblBld.Label, Master:sMaster, BuildID: oLblBld.BuildID, Duration: oLblBld.Duration, DurationString: oLblBld.DurationString, Environment: oLblBld.Environment, EstimatedDuration: oLblBld.EstimatedDuration, TimeSpentInQueue: oLblBld.TimeSpentInQueue, BuildStuck: oLblBld.BuildStuck, BuildParked: oLblBld.BuildParked, ExternalizableId: oLblBld.ExternalizableId, FullDisplayName: oLblBld.FullDisplayName, HasArtifacts: oLblBld.HasArtifacts, Number: oLblBld.Number, Parent: oLblBld.Parent, QueueId: oLblBld.QueueId, Result: oLblBld.Result, RootDir: oLblBld.RootDir, SearchUrl: oLblBld.SearchUrl, StartTimeInMillis: oLblBld.StartTimeInMillis, Time: oLblBld.Time, TimeInMillis: oLblBld.TimeInMillis, TimestampString: oLblBld.TimestampString, TimestampString2: oLblBld.TimestampString2, Url: oLblBld.Url, BuildSCM: oLblBld.SCMs, ChangeSets: oLblBld.ChangeSets, CulpritIDs: oLblBld.CulpritIDs, Culprits: oLblBld.Culprits, LogFile: oLblBld.LogFile, LogLength: oLblBld.LogLength, AllowKill: oLblBld.AllowKill, AllowTerm: oLblBld.AllowTerm, StartedYet: oLblBld.StartedYet, IsBuilding: oLblBld.IsBuilding, InProgress: oLblBld.InProgress, LogUpdated: oLblBld.LogUpdated, BuildAllActions: oLblBld.BuildAllActions];

                    //Debugging---
                    //steps.echo("${splunkJSON}")

                    def data = steps.readJSON text: JsonOutput.toJson(splunkJSON)
                    //steps.echo("${data}")
                    steps.writeJSON('splunk_data.json', data, 4)
                    def jsonFileString = steps.readFile('splunk_data.json')
                    //steps.echo("${jsonFileString}")
                    try {
                        def jsonLabels = new groovy.json.JsonBuilder()
                        jsonLabels "LabelStats":splunkJSON
                        //steps.echo("${jsonLabels}")
                        //int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data \'${jsonFileString}\'")
                        int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data \'${jsonLabels}\'")
                        println "Return code - ${returnStatus}"
                    } catch (Exception excp) {
                        steps.echo "Exception raised when using CURL command - ${excp.message}"
                    }
                }

                steps.echo("Finished posting data!")

//                try {
//                    int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data \'${jsonLabels}\'")
//                    println "Return code - ${returnStatus}"
//                } catch (e) {
//                    steps.echo "Exception raised when using CURL command - ${e.message}"
//                }
            }

            return outputStream.toString();
        }
        catch(StringIndexOutOfBoundsException sioobexp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - StringIndexOutOfBoundsException raised - ${sioobexp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("StringIndexOutOfBoundsException - \"${strExp}\"")
            }
            return strExp;
        }
        catch (Exception exp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - EXCEPTION raised - ${exp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("Exception - \"${strExp}\"")
            }
            return strExp;
        }
    }

    def PostJsonToSplunk(def sMaster)
    {
        def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor();

        try {

            def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/raw";
            def strToken = "a0ec61b4-cb16-4a62-a159-9c3982d11153"; //Token used by Jenkins App

            def outputStream = new StringBuffer();

            steps.node("PROD-LIN7") {
                jsonData.each { e ->
                    RoleData oRoleMbr = e
                    Map splunkJSON = [Folder: oRoleMbr.Folder, IsParentFolder: oRoleMbr.IsParentFolder, Group: oRoleMbr.Group, Role: oRoleMbr.Role, ADGroup: oRoleMbr.ADGroup, IsUser: oRoleMbr.IsUser, Count: oRoleMbr.Count, Master: sMaster, TopFolder: oRoleMbr.TopFolder?:"", ParentFolder: oRoleMbr.ParentFolder?:"", Members: oRoleMbr.Members?:""]

                    def data = steps.readJSON text: JsonOutput.toJson(splunkJSON)
                    steps.writeJSON('splunk_role.json', data)

                    def jsonFileString = steps.readFile('splunk_role.json')
                    try {
                        def jsonLabels = new groovy.json.JsonBuilder()
                        jsonLabels "RoleMbrStats":splunkJSON
                        String sRoleJson = "${jsonLabels.toString()}"
                        int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data \'${sRoleJson}\'")
                        println "Return code - ${returnStatus}"
                    } catch (Exception excp) {
                        //steps.echo "RoleStats exception raised when using CURL command - ${excp.message}"
                    }
                }
            }

            return outputStream.toString();
        }
        catch(StringIndexOutOfBoundsException sioobexp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - StringIndexOutOfBoundsException raised - ${sioobexp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("StringIndexOutOfBoundsException - \"${strExp}\"")
            }
            return strExp;
        }
        catch (Exception exp)
        {
            def expDate = new Date()
            String strExp = sdf.format(expDate) + " :: [StatsCollector] - EXCEPTION raised - ${exp.message}"
            steps.node("PROD-LIN7") {
                steps.echo("Exception - \"${strExp}\"")
            }
            return strExp;
        }
    }

    @NonCPS
    def WriteData(def splunkJson) {
        def data = steps.readJSON text: JsonOutput.toJson(splunkJson)
        steps.echo("${data}")
        steps.echo("Lets write the JSON to a file")
        steps.writeJSON('splunk_role.json', data, 4)
    }

    def NativeGroovyPost(def sMsg)
    {
        try {
            def post = new URL("https://hec.splunk.mclocal.int:13510/services/collector/raw").openConnection();
            def message = sMsg;
            post.setRequestMethod("POST")
            post.setRequestProperty("Authorization","a0ec61b4-cb16-4a62-a159-9c3982d11153" )
            post.setDoOutput(true)
            post.setRequestProperty("Content-Type", "application/json")
            post.getOutputStream().write(message.getBytes("UTF-8"));
            steps.echo("Sending ${message}")
            def postRC = post.getResponseCode();
            steps.echo(postRC);
            if (postRC.equals(200)) {
                steps.echo(post.getInputStream().getText());
            }

        }
        catch (Exception excp) {
            steps.echo "Exception raised when posting using native groovy POST command - ${excp.message}"
        }
    }
}