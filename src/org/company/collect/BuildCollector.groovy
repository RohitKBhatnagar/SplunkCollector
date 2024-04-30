package org.company.collect

import jenkins.model.Jenkins

import org.company.IStepExecutor
import org.company.ioc.ContextRegistry

//General idea is to keep our vars scripts [like CollectBuildCountsInLastHour etc] as simple as possible and do all the work inside a unit-testable class like this one here!
class BuildCollector implements Serializable
{
    private String _solutionPath

    //Please note, we use sh, batch and error steps in our class, but instead of using them directly, we use the ContextRegistry to get an instance of IStepExecutor to call Jenkins steps with that. This way, we can swap out the context when we want to unit test the collectHrCountsViaSh() or collectHrCountsViaBat() methods as an example here  later.

    BuildCollector(String solutionPath)
    {
        _solutionPath = solutionPath
    }

    void collectHrCountsViaSh()
    {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()
        try
        {
            int returnStatus = steps.sh("echo \"Collecting statistics for ${this._solutionPath}...\"")
            if (returnStatus != 0)
            {
                steps.error("Some error was reported when executing shell script...")
            }
        }
        catch (Exception exp)
        {
            steps.error("BuildCollector.collectHrCountsViaSh:: Exception generated - " + exp.message)
        }
    }

    void collectHrCountsViaBat()
    {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()
        try
        {
            int returnStatus = steps.bat("echo \"Collecting statistics for ${this._solutionPath}...\"")
            if (returnStatus != 0)
            {
                steps.error("Some error was reported when executing batch command...")
            }
        }
        catch (Exception exp)
        {
            steps.error("BuildCollector.collectHrCountsViaBat:: Exception generated - " + exp.message)
        }
    }
}
