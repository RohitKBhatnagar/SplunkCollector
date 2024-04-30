package org.company

class StepExecutor implements IStepExecutor
{
    // this will be provided by the vars script and
    // let's us access Jenkins steps
    private _steps

    StepExecutor(steps)
    {
        this._steps = steps
    }

    @Override
    int sh(String command)
    {
        try
        {
            this._steps.sh returnStatus: true, script: "${command}"
            return 0
        }
        catch (Exception exp)
        {
            this._steps.error("StepExecutor:sh:: Exception generated - " + exp.message)
            return -1;
        }
    }

    @Override
    int bat(String command)
    {
        try
        {
            this._steps.bat returnStatus: true, script: "${command}"
            return 0
        }
        catch (Exception exp)
        {
            this._steps.error("StepExecutor:bat:: Exception generated - " + exp.message)
            return -1;
        }
    }

    @Override
    void error(String message)
    {
        try
        {
            this._steps.error(message)
        }
        catch (Exception exp)
        {
            println "StepExecutor:error:: Exception generated - ${exp.message}"
        }
    }

    @Override
    void stage(String label, Closure body) {
        this._steps.stage(label) {
            body()
        }
    }

    @Override
    void node(String agentLabel, Closure body) {
        this._steps.node(agentLabel) {
            body()
        }
        //body.call()
    }

    @Override
    void pipeline(Closure body) {
        this._steps.pipeline() {
            body()
        }
    }

    @Override
    void agent(String sAgent, Closure body) {
        this._steps.node(sAgent) {
            body()
        }
    }

    @Override
    void environment(ArrayList<String> lstEnv, Closure body) {
        this._steps.environment(lstEnv) {
            body()
        }
    }

    @Override
    void withEnv(ArrayList<String> lstEnv, Closure body) {
        this._steps.withEnv(lstEnv) {
            body()
        }
    }

    @Override
    void dir(String dirPath, Closure body) {
        this._steps.dir(dirPath) {
            body()
        }
    }

    @Override
    void git(String gitRepo) {
        this._steps.git(gitRepo)
    }

    @Override
    void steps(Closure body) {
        this._steps.steps() {
            body.call()
        }
    }

    @Override
    void post(Closure body) {
        this._steps.post() {
            body.call()
        }
    }

    @Override
    void always(Closure body) {
        this._steps.always() {
            body.call()
        }
    }

//    @Override
//    void emailext(String eBody, /*List<RecipientProvider>*/ String eRecipientProviders, String eSubject, Closure body) {
//        this._steps.emailext(eBody, eRecipientProviders, eSubject) {
//            body.call()
//        }
//    }

    @Override
    void writeJSON(def myFile, def myJson, def myPretty = 4)
    {
        this._steps.writeJSON(file: myFile, json: myJson, pretty: myPretty)
    }

    @Override
    void readFile(def ReadFile, def optionalEncoding = "")
    {
        this._steps.readFile(file: ReadFile, encoding: optionalEncoding)
    }

    @Override
    void echo(def strMsg)
    {
        this._steps.echo(strMsg)
    }

    @Override
    def readJSON(def text)
    {
        return this._steps.readJSON(text)
    }
}
