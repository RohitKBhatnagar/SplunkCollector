package vars

import jenkins.model.Jenkins
import groovy.time.TimeCategory
import org.company.collect.BuildCollector
import org.company.ioc.ContextRegistry

//This is our implementation of our shared library variable 'CollectBuildCountsInLastHour'.
//First, we set the context with the context registry. Since we are not in a unit test, we use the default context. The 'this' instance we pass into registerDefaultContext() will be saved by the DefaultContext inside its private _steps variable and is used to access Jenkins steps. After registering the context, we are free to instantiate our BuildCollector class and call the collectHrCountsViaSh() method doing all the work.

class CollectBuildCountsInLastHour
{
    //ORG def call(String solutionPath)
    def call(def topItems = Jenkins.get().getItems())
    {
        def iItemCount = 1
        def iJobCount = 1
        def iTotalBuilds = 0
        def iTotalItems = 0
        def iIsBuilding = 0
        def iOlderThan60Mins = 0
        def iNoBuilds = 0 //These should fall under NULLPointerExceptions
        def iLastHrBuilds = 0 //Build counts in last hr only

        try
        {
            /*ContextRegistry.registerDefaultContext(this)

            def collectStats = new BuildCollector(solutionPath)
            collectStats.collectHrCountsViaSh()*/
            topItems.each
                    {
                        topI ->
                            iTotalItems++ //Counts total top level items
                            //println "${iItemCount++} | ${topI.getName()}"
                            //if (!topItems.disabled)
                            //{
                            def allItemJobs = topI.getAllJobs() //https://docs.huihoo.com/javadoc/jenkins/1.512/hudson/model/Job.html
                            iJobCount = 1
                            def String strPrint = ""
                            allItemJobs.each
                                    {
                                        itemJob ->
                                            try
                                            {
                                                //if(allItemJobs.getLastBuild() != null)
                                                //{
                                                iTotalBuilds++ //Counts total builds in entire master
                                                if(itemJob.building)
                                                    iIsBuilding++;
                                                def build_time = itemJob.getLastBuild().startTimeInMillis;
                                                //println "${build_time} - ${new Date(build_time)}"
                                                def before60Mins
                                                use( TimeCategory )
                                                        {
                                                            before60Mins = new Date() - 60.minutes
                                                            //println "${before60Mins}"
                                                        }
                                                if(!new Date(build_time).before(before60Mins))
                                                {
                                                    iLastHrBuilds++
                                                    def String jobResult =  itemJob.getLastBuild().result;
                                                    strPrint = "DETAILS | ${itemJob.getLastBuild()} | Time | ${itemJob.getLastBuild().getTime()} | Result | ${itemJob.getLastBuild().result} | Duration | ${itemJob.getLastBuild().durationString} | Duration(ms) | ${itemJob.getLastBuild().getDuration()}"
                                                    //println "${iItemCount++} | ${topI.getName()} | ${iJobCount++} | ${itemJob.getName()} | ${strPrint}"
                                                    System.out.println "ITEMS | ${iItemCount++} | ${topI.getName()} | ${iJobCount++} | ${itemJob.getName()} | ${strPrint}"
                                                }
                                                else
                                                    iOlderThan60Mins++
                                                //}
                                            }
                                            catch(NullPointerException npexp)
                                            {
                                                iNoBuilds++
                                                ////println "\t\t\t${iJobCount++} | ${itemJob.getName()} | ${npexp.message}"
                                                //println "${iItemCount++} | ${topI.getName()} | ${iJobCount++} | ${itemJob.getName()} | NULL-POINTER EXCEPTION | ${npexp.message}"
                                            }
                                            catch(Exception exp)
                                            {
                                                ////println "\t\t\t\t${iJobCount++} | ${itemJob.getName()} | ${exp.message}"
                                                println "${iItemCount++} | ${topI.getName()} | ${iJobCount++} | ${itemJob.getName()} | GENERIC EXCEPTION | ${exp.message}"
                                            }
                                    }
                            //}
                    }

            println "ITEMS Totals | TOTAL ITEMS | ${iTotalItems} | TOTAL BUILDS | ${iTotalBuilds} | OLD BUILDS | ${iOlderThan60Mins} | BUILDING | ${iIsBuilding} | NO BUILDS | ${iNoBuilds} | LAST HR BUILDS | ${iLastHrBuilds}"
        }
        catch (Exception exp)
        {
            println "CollectBuildCountsInLastHour.call:: Exception generated - ${exp.message}"
        }
    }
}
