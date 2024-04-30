package org.company.ioc

//To finish up our basic dependency injection setup, adding a "context registry" that is used to store the current context (DefaultContext during normal execution and a Mockito mock of IContext during unit tests)

class ContextRegistry implements Serializable
{
    private static IContext _context

    static void registerContext(IContext context)
    {
        try
        {
            _context = context
        }
        catch (Exception exp)
        {
            println("ContextRegistry:registerContext:: Exception generated - " + exp.message)
            _context = null
        }
    }

    static void registerDefaultContext(Object steps)
    {
        try
        {
            _context = new DefaultContext(steps)
        }
        catch (Exception exp)
        {
            println("ContextRegistry:registerDefaultContext:: Exception generated - " + exp.message);
            _context = null;
        }
    }

    static IContext getContext()
    {
        return _context
    }
}
