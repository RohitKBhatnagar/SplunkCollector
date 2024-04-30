package org.company

interface IStepExecutor {
    int sh(String command)
    int bat(String command)
    void error(String message)
    void stage(String label, Closure body)
    void node(String agentLabel, Closure body)
    void environment(ArrayList<String> lstEnv, Closure body)
    void pipeline(Closure body)
    void agent(String sAgent, Closure body)
    void withEnv(ArrayList<String> lstEnv, Closure body)
    void dir(String dirPath, Closure body)
    void git(String gitRepo)
    void steps(Closure body)
    void post(Closure body)
    void always(Closure body)
    //void emailText(String eBody, String eRecipientProviders, String eSubject, Closure body)
    void writeJSON(def myFile, def myJson, def myPretty)
    void readFile(def ReadFile, def optionalEncoding)
    def readJSON(def optionalText)
    void echo(def strMsg)
    // Please add more methods for respective steps, if needed
}