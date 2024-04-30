package org.company.ioc

import org.company.IStepExecutor

//Because we don't want to use the implementation of StepExecutor and IStepExecutor in our unit tests, we will setup some basic dependency injection in order to swap the above implementation with a mock during unit tests.
//This interface will be mocked for our unit tests
interface IContext
{
    IStepExecutor getStepExecutor()
}