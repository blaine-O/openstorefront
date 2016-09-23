/*
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.core;

import edu.usu.sdl.core.init.ApplyOnceInit;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.core.view.ManagerView;
import edu.usu.sdl.openstorefront.service.io.AttributeImporter;
import edu.usu.sdl.openstorefront.service.io.HelpImporter;
import edu.usu.sdl.openstorefront.service.io.LookupImporter;
import edu.usu.sdl.openstorefront.service.manager.AsyncTaskManager;
import edu.usu.sdl.openstorefront.service.manager.DBLogManager;
import edu.usu.sdl.openstorefront.service.manager.DBManager;
import edu.usu.sdl.openstorefront.service.manager.JiraManager;
import edu.usu.sdl.openstorefront.service.manager.JobManager;
import edu.usu.sdl.openstorefront.service.manager.LDAPManager;
import edu.usu.sdl.openstorefront.service.manager.MailManager;
import edu.usu.sdl.openstorefront.service.manager.OSFCacheManager;
import edu.usu.sdl.openstorefront.service.manager.OsgiManager;
import edu.usu.sdl.openstorefront.service.manager.PluginManager;
import edu.usu.sdl.openstorefront.service.manager.ReportManager;
import edu.usu.sdl.openstorefront.service.manager.SearchServerManager;
import edu.usu.sdl.openstorefront.service.manager.UserAgentManager;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.truevfs.access.TVFS;
import net.sourceforge.stripes.util.ResolverUtil;

/**
 * Used to init the application
 *
 * @author dshurtleff
 */
public class CoreSystem
{

	private static final Logger log = Logger.getLogger(CoreSystem.class.getName());

	private static AtomicBoolean started = new AtomicBoolean(false);

	private CoreSystem()
	{
	}

	//Order is important
	private static List<Initializable> managers = Arrays.asList(
			new PropertiesManager(),
			new OsgiManager(),
			new FileSystemManager(),
			new DBManager(),
			new SearchServerManager(),
			new OSFCacheManager(),
			new JiraManager(),
			new LookupImporter(),
			new AttributeImporter(),
			new MailManager(),
			new JobManager(),
			new UserAgentManager(),
			new AsyncTaskManager(),
			new ReportManager(),
			new LDAPManager(),
			new HelpImporter(),
			new DBLogManager(),
			new PluginManager()
	);

	public static void startup()
	{
		managers.forEach(manager -> {
			startupManager(manager);
		});

		//Apply any Inits
		ResolverUtil resolverUtil = new ResolverUtil();
		resolverUtil.find(new ResolverUtil.IsA(ApplyOnceInit.class), "edu.usu.sdl.core.init");
		for (Object testObject : resolverUtil.getClasses()) {
			Class testClass = (Class) testObject;
			try {
				if (ApplyOnceInit.class.getSimpleName().equals(testClass.getSimpleName()) == false) {
					((ApplyOnceInit) testClass.newInstance()).applyChanges();
				}
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new OpenStorefrontRuntimeException(ex);
			}
		}

		started.set(true);
	}

	private static void startupManager(Initializable initializable)
	{
		log.log(Level.INFO, MessageFormat.format("Starting up:{0}", initializable.getClass().getSimpleName()));
		initializable.initialize();
	}

	private static void shutdownManager(Initializable initializable)
	{
		//On shutdown we want it to roll through
		log.log(Level.INFO, MessageFormat.format("Shutting down:{0}", initializable.getClass().getSimpleName()));
		try {
			initializable.shutdown();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to Shutdown: " + initializable.getClass().getSimpleName(), e);
		}
	}
	
	public static List<ManagerView> getManagersView() 
	{
		List<ManagerView> views = new ArrayList<>();
		
		int order = 1;
		for (Initializable manager : managers) {
			ManagerView view = new ManagerView();
			view.setName(manager.getClass().getSimpleName());
			view.setManagerClass(manager.getClass().getName());
			view.setOrder(order++);
			view.setStarted(manager.isStarted());
			views.add(view);
		}
		
		return views;
	}
	
	public static void startManager(String managerClassName)
	{
		Initializable manager = findManager(managerClassName, false);
		if (manager.isStarted() == false) {
			manager.initialize();
		} 
	}
	
	public static Initializable findManager(String managerClassName, boolean skipFoundCheck) 
	{
		Initializable foundManger = null;
		for (Initializable manager : managers) {
			if (manager.getClass().getName().equals(managerClassName)) {
				foundManger = manager;
				break;
			}
		}		
		
		if (foundManger == null && !skipFoundCheck) {
			throw new OpenStorefrontRuntimeException("Unable to find manger: " + managerClassName, "Check input");
		}
		
		return foundManger;
	}
	
	public static void stopManager(String managerClassName)
	{		
		Initializable manager = findManager(managerClassName, false);		
		if (manager.isStarted()) {
			manager.shutdown();
		} 		
	}

	public static void restartManager(String managerClassName)
	{		
		//pause scheduler reduce auto process interference
		Initializable jobManager = findManager(JobManager.class.getName(), false);
		started.set(false);
		
		if (jobManager.isStarted()) {
			JobManager.pauseScheduler();
		}		
		try {
			stopManager(managerClassName);
			startManager(managerClassName);		
		} catch (Exception e) {			
			throw new OpenStorefrontRuntimeException("Unable to restart manager: " + managerClassName, "Confirm application state and try or if system is in a bad state. Restart application.");
		}
				
		if (jobManager.isStarted()) {
			JobManager.resumeScheduler();
		}
		started.set(true);
	}

	public static void shutdown()
	{
		try {
			log.log(Level.INFO, "Unmount Truevfs");
			TVFS.umount();
		} catch (Exception e) {
			log.log(Level.SEVERE, MessageFormat.format("Failed to unmount: {0}", e.getMessage()));
		}

		//Shutdown in reverse order to make sure the dependancies are good.
		Collections.reverse(managers);
		managers.forEach(manager -> {
			shutdownManager(manager);
		});
		Collections.reverse(managers);

		started.set(false);
	}

	public static boolean isStarted()
	{
		boolean running = false;
		if (started.get()) {
			running = true;
		}
		return running;
	}

	public static void restart()
	{
		if (isStarted()) {
			shutdown();
		}
		startup();
	}

}
