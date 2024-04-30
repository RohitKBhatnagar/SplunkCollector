package vars

import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonOutput
import hudson.model.Executor
import org.company.collect.LabelBuild
import org.company.collect.PostSplunkData
import org.company.collect.RoleData
import org.company.ioc.ContextRegistry

/// Pull out various Groups, Roles and Members script to be run as a pipeline job
/// Author: Rohit K. Bhatnagar
/// Created By: Rohit K. Bhatnagar
/// Created On: January 17, 2024
/// Modified On: January 31, 2024
/// Modified Reason: Adding support for pulling out all top-level-folders and pull out of all members against ADGroups

import java.text.SimpleDateFormat

import jenkins.model.Jenkins
import nectar.plugins.rbac.groups.*;
import com.cloudbees.hudson.plugins.folder.AbstractFolder;

def call(def jenkinsTopItems = Jenkins.getInstanceOrNull().getItems(), def String strMaster = java.net.InetAddress.getLocalHost())
{
    def sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
    def startDate = new Date()
    println "Start time : ${sdf.format(startDate)}"

    println "Starting on - ${strMaster}"
    def ControllerName = Jenkins.instance.rootUrl.split('/')[2].split('.company.int')[0]
    println "--------------------------"
    def topItems = jenkinsTopItems
    println "Total Top Items - ${topItems.size()}"
    try
    {
        ArrayList<RoleData> lstRoleMbrs = new ArrayList<RoleData>();
        topItems.eachWithIndex
        { itTop, iTopCount ->
            Map containers = new TreeMap();
            if(Jenkins.instance.getItemByFullName(itTop.name, AbstractFolder) != null)
            {
                containers.put(Jenkins.instance.displayName, GroupContainerLocator.locate(Jenkins.instance.getItemByFullName(itTop.name, AbstractFolder)));
                for (i in Jenkins.instance.getItemByFullName(itTop.name, AbstractFolder).allItems)
                    if (GroupContainerLocator.isGroupContainer(i.getClass())) {
                        GroupContainer g = GroupContainerLocator.locate(i);
                        if (g != null)
                            containers.put(Jenkins.instance.displayName + "/" + i.fullDisplayName, g);
                    }
            }

            containers.eachWithIndex
            {
                itC, itCount ->
                for (c in itC)
                {
                    if(c.value.groups.size() > 0)
                    {
                        Boolean bIsParent = true;
                        if(topItems.contains(c.value.containerTarget.getTarget()))
                            bIsParent = true
                        else
                            bIsParent = false
                        def lstGroups = []
                        for (g in c.value.groups)
                        {
                            def lstRoles = []
                            for (r in g.roles)
                            {
                                def lstMembers = []
                                for (a in g.membership)
                                {
                                    Boolean bIsUser = false
                                    def lstADMembers = []
                                    def total = 0
                                    if(a instanceof nectar.plugins.rbac.assignees.UserAssignee)
                                        bIsUser = true
                                    if(a instanceof nectar.plugins.rbac.assignees.ExternalGroupAssignee)
                                    {
                                        if(a.hasProperty('members'))
                                        {
                                            for (b in a.members) {
                                                lstADMembers.add(b);
                                            }
                                            total = a.members.size()
                                        }
                                        else
                                            total = 0
                                    }
                                    else
                                        total = 0;
                                    if(bIsUser)
                                        lstMembers.add(ADGroup:a.id, IsUser:"true");
                                    else
                                    {
                                        if(lstADMembers.size() > 0)
                                            lstMembers.add(ADGroup:a.id, IsUser:"false", Members:lstADMembers, Count:total);
                                        else
                                            lstMembers.add(ADGroup:a.id, IsUser:"false", Count:total);
                                    }
                                }
                                lstRoles.add(Role:r + (g.doesPropagateToChildren(r) ? " (and children)" : " (pinned)"), ADGroups:lstMembers)
                            }
                            if(lstRoles.size() > 0)
                                lstGroups.add(Group:g.name, Roles:lstRoles)
                            else
                                lstGroups.add(Group:g.name)
                        }
                        String sParent = ""
                        try{
                            sParent = c.value.containerTarget.fullName.substring(0, c.value.containerTarget.fullName.indexOf("/"))
                        }
                        catch(Exception exp) {
                            sParent = ""
                        }
                        def lstFolders;
                        if(bIsParent)
                            lstFolders = [Folder:c.value.containerTarget.fullName, IsParentFolder:bIsParent, Groups:lstGroups]
                        else
                            lstFolders = [Folder:c.value.containerTarget.fullName, IsParentFolder:bIsParent, TopFolder:sParent, ParentFolder:c.value.containerTarget.getParent().fullName, Groups:lstGroups]

                        //+++++++++++++++++++++++++++++++++++++++++++++++++++++
                        def tmpMap = [:]
                        Boolean bMembers = true
                        lstFolders.each
                        {
                            key, val->
                            if(key == "Groups")
                            {
                                val.each
                                { lstGrps->
                                    lstGrps.each
                                    { grpKey, grpVal->
                                        if(grpKey == "Roles")
                                        {
                                            grpVal.each
                                            {
                                                lstRoles->
                                                    lstRoles.each
                                                    { roleKey, roleVal->
                                                        if(roleKey == "ADGroups")
                                                        {
                                                            roleVal.each
                                                            { lstADs->
                                                                def tmpADMap = [:]
                                                                lstADs.each
                                                                { adKey, adVal->
                                                                    tmpADMap.put(adKey, adVal)
                                                                    if(adKey == "Members")
                                                                    {
                                                                        bMembers = false
                                                                        def tmpMembers = []
                                                                        adVal.each
                                                                        { mbrVal->
                                                                            tmpMembers.add(mbrVal)
                                                                        }
                                                                        tmpMap.put("Members", tmpMembers)
                                                                    }
                                                                }
                                                                tmpMap.putAll(tmpADMap)
                                                                def myRoleMembers = new RoleData();
                                                                tmpMap.each { myKey, myValue ->
                                                                    myRoleMembers."${myKey}" = myValue
                                                                }
                                                                lstRoleMbrs.add(myRoleMembers);
                                                            }
                                                        }
                                                        else
                                                            tmpMap.put(roleKey, roleVal)
                                                    }
                                            }
                                        }
                                        else
                                            tmpMap.put(grpKey, grpVal)
                                    }
                                }
                            }
                            else
                                tmpMap.put(key, val)
                        }
                        if(bMembers) {
                            def myRoleMembers = new RoleData()
                            tmpMap.each { myKey, myValue ->
                                myRoleMembers."${myKey}" = myValue
                            }
                            lstRoleMbrs.add(myRoleMembers)
                        }
                    }
                }
            }
        }
        def oPostJson = new PostSplunkData(lstRoleMbrs, this);
        ContextRegistry.registerDefaultContext(this);
        def strOut = oPostJson.PostJsonToSplunk(ControllerName)
        println(strOut)
    }
    catch(StringIndexOutOfBoundsException sioobe)
    {
        println "STRING INDEX OUT OF BOUNDS EXCEPTION - ${sioobe.message}"
    }
    catch(Exception exp)
    {
        println "EXCEPTION - ${exp.message}"
    }

//--------------------------------------------------
    def endDate = new Date()
    println "End time: ${sdf.format(endDate)}"

    return
}