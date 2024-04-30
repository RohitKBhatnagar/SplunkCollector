package org.company.ioc

import org.company.IStepExecutor
import org.company.StepExecutor

//For regular execution of our StatsCollector library we still need a default implementation
class DefaultContext implements IContext, Serializable
{
    // the same as in the StepExecutor class
    private _steps

    DefaultContext(steps)
    {
        this._steps = steps
    }

    @Override
    IStepExecutor getStepExecutor()
    {
        try
        {
            return new StepExecutor(this._steps)
        }
        catch (Exception exp)
        {
            println("DefaultContext:getStepExecutor:: Exception generated - " + exp.message)
            return null
        }
    }
}
