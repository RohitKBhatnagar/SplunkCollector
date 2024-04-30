import groovy.json.JsonOutput;
import groovy.json.JsonBuilder;
import com.cloudbees.opscenter.server.model.ConnectedMaster

def call() {
    def JenkinsURL
    def strSplunk = "https://hec.splunk.mclocal.int:13510/services/collector/raw";
    def downloadedJsonFile = 'downloaded-data.json'
    def splunkDataFile = 'splunkFinal.json'
    def credMap = [
        "cd.company.int": "cd_metrics_key",
        "cd2.company.int": "cd2_metrics_key",
        "cd3.company.int": "cd3_metrics_key",
        "cd4.company.int": "cd4_metrics_key",
        "cd5.company.int": "cd5_metrics_key"
        ]

    credMap.each { controller, JenkinsCred ->
        if (JenkinsCred){
            JenkinsURL = "https://" + controller + "/jenkins"
            node () {
                doMetrcsDownload(JenkinsURL, JenkinsCred, downloadedJsonFile)
                processJsonData(downloadedJsonFile, splunkDataFile)
                uploadDataToSplunk(strSplunk, splunkDataFile)
            }  
        }
    }
}

def doMetrcsDownload(JenkinsURL, JenkinsCred, downloadedJsonFile) {
    try {
        deleteDir()
        withCredentials([string(credentialsId: JenkinsCred, variable: 'JenKey')]) {
            sh "curl $JenkinsURL/metrics/$JenKey/metrics?pretty=true -o ${downloadedJsonFile}"
        }
    } catch (Exception e) {
        println "EXCEPTION: ${e.message}"
    }
}

def processJsonData(downloadedJsonFile, splunkDataFile) {
    try {
        json_string = readFile(downloadedJsonFile).trim()
        metricsData = readJSON text: json_string
        def jsonLabels = new groovy.json.JsonBuilder()
        jsonLabels "JenkinsMetrics":metricsData
        new File("$env.WORKSPACE", splunkDataFile) << jsonLabels
        def toDelFile = new File("$env.WORKSPACE",downloadedJsonFile)
        toDelFile.delete()
    } catch (Exception e) {
        def toDelFile = new File("$env.WORKSPACE",downloadedJsonFile)
        toDelFile.delete()
        println "EXCEPTION: ${e.message}"
    }
}

def uploadDataToSplunk(strSplunk, splunkDataFile) {
    try {
        withCredentials([string(credentialsId: 'splunk_token_jenkins_app', variable: 'strToken')]) {
            int returnStatus = steps.sh("curl -f -k ${strSplunk} -H \"Authorization: Splunk ${strToken}\" --data @${splunkDataFile}")
        }
        deleteDir()
    } catch (e) {
        deleteDir()
        println "Error message from Curl: ${e.message}"
    }
}