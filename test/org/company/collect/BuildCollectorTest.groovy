package org.company.collect

import jenkins.model.Jenkins

import org.company.IStepExecutor;
import org.company.ioc.ContextRegistry;
import org.company.ioc.IContext;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

//We have created a new test class BuildCollectorTest inside the test folder with package org.company.collect Before every test, we use Mockito to mock the IContext and IStepExecutor interfaces and register the mocked context. Then we can simply create a new BuildCollector instance in our test and verify the behaviour of our collectHrCountsViaSh() or collectHrCountsViaBat() as an example methods [You can add more methods as neccessary for your unit tests below]. The full test class with two example test is implemented below:

class BuildCollectorTest
{
    private IContext _context;
    private IStepExecutor _steps;

    @Before
    public void setup()
    {
        try
        {
            _context = mock(IContext.class);
            _steps = mock(IStepExecutor.class);

            when(_context.getStepExecutor()).thenReturn(_steps);

            ContextRegistry.registerContext(_context);
        }
        catch (Exception exp)
        {
            println "BuildCollectorTest.setup:: Exception generated - ${exp.message}"
        }
    }

    @Test
    public void collectHrCountsViaSh_callsShStep()
    {
        try {
            // prepare
            String solutionPath = "some/path/to.sln";
            BuildCollector bldCollector = new BuildCollector(solutionPath);

            // execute
            bldCollector.collectHrCountsViaSh();

            // verify
            verify(_steps).sh(anyString());
        }
        catch (Exception exp)
        {
            println "BuildCollectorTest.collectHrCountsViaSh_callsShStep:: Exception generated - ${exp.message}"
        }
    }

    @Test
    public void collectHrCountsViaSh_shStepReturnsStatusNotEqualsZero_callsErrorStep()
    {
        try
        {
            // prepare
            String solutionPath = "some/path/to.sln";
            BuildCollector bldCollector = new BuildCollector(solutionPath);

            when(_steps.sh(anyString())).thenReturn(-1);

            // execute
            bldCollector.collectHrCountsViaSh();

            // verify
            verify(_steps).error(anyString());
        }
        catch (Exception exp)
        {
            println "BuildCollectorTest.collectHrCountsViaSh_shStepReturnsStatusNotEqualsZero_callsErrorStep:: Exception generated - ${exp.message}"
        }
    }
}
