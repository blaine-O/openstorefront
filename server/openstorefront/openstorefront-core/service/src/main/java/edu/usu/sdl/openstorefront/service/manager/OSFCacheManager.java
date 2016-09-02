/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.service.manager;

import edu.usu.sdl.openstorefront.common.manager.Initializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;

/**
 * Handling Application level caching
 *
 * @author dshurtleff
 */
public class OSFCacheManager
		implements Initializable
{

	private static final Logger log = Logger.getLogger(OSFCacheManager.class.getName());

	public static final String ALLCODE_KEY = "ALLCODES";

	private static Cache lookupCache;
	private static Cache attributeCache;
	private static Cache attributeTypeCache;
	private static Cache attributeCodeAllCache;
	private static Cache userAgentCache;
	private static Cache componentCache;
	private static Cache componentLookupCache;
	private static Cache componentApprovalCache;
	private static Cache componentTypeCache;
	private static Cache applicationCache;
	private static Cache contactCache;
	private static Cache searchCache;
	
	private static AtomicBoolean started = new AtomicBoolean(false);

	public static void init()
	{
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		try {
			Configuration config = new Configuration();
			config.setUpdateCheck(false);
			config.setName("Main");
			CacheManager singletonManager = CacheManager.create(config);
			
			Cache memoryOnlyCache = new Cache("lookupCache", 500, false, false, 600, 600);
			singletonManager.addCache(memoryOnlyCache);
			lookupCache = singletonManager.getCache("lookupCache");

			memoryOnlyCache = new Cache("attributeCache", 500, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			attributeCache = singletonManager.getCache("attributeCache");

			memoryOnlyCache = new Cache("attributeTypeCache", 500, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			attributeTypeCache = singletonManager.getCache("attributeTypeCache");

			memoryOnlyCache = new Cache("attributeCodeAllCache", 1, false, true, 0, 0);
			singletonManager.addCache(memoryOnlyCache);
			attributeCodeAllCache = singletonManager.getCache("attributeCodeAllCache");

			memoryOnlyCache = new Cache("userAgentCache", 100, false, false, 7200, 7200);
			singletonManager.addCache(memoryOnlyCache);
			userAgentCache = singletonManager.getCache("userAgentCache");

			memoryOnlyCache = new Cache("componentCache", 200, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			componentCache = singletonManager.getCache("componentCache");

			memoryOnlyCache = new Cache("componentLookupCache", 50000, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			componentLookupCache = singletonManager.getCache("componentLookupCache");

			memoryOnlyCache = new Cache("componentApprovalCache", 50000, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			componentApprovalCache = singletonManager.getCache("componentApprovalCache");

			memoryOnlyCache = new Cache("componentTypeCache", 1, false, false, 300, 300);
			singletonManager.addCache(memoryOnlyCache);
			componentTypeCache = singletonManager.getCache("componentTypeCache");

			memoryOnlyCache = new Cache("applicationCache", 100, false, true, 0, 0);
			singletonManager.addCache(memoryOnlyCache);
			applicationCache = singletonManager.getCache("applicationCache");

			memoryOnlyCache = new Cache("contactCache", 5000, false, false, 1800, 1800);
			singletonManager.addCache(memoryOnlyCache);
			contactCache = singletonManager.getCache("contactCache");
			
			memoryOnlyCache = new Cache("searchCache", 50, false, false, 1800, 1800);
			singletonManager.addCache(memoryOnlyCache);
			searchCache = singletonManager.getCache("searchCache");			
						
		} finally {
			lock.unlock();
		}

	}
	
	public static CacheManager getCacheManager()
	{
		return CacheManager.getInstance();
	}

	public static void cleanUp()
	{
		CacheManager.getInstance().shutdown();
	}

	public static Cache getLookupCache()
	{
		return lookupCache;
	}

	public static Cache getAttributeCache()
	{
		return attributeCache;
	}

	public static Cache getAttributeTypeCache()
	{
		return attributeTypeCache;
	}

	public static Cache getUserAgentCache()
	{
		return userAgentCache;
	}

	public static Cache getComponentCache()
	{
		return componentCache;
	}

	public static Cache getComponentLookupCache()
	{
		return componentLookupCache;
	}

	public static Cache getAttributeCodeAllCache()
	{
		return attributeCodeAllCache;
	}

	public static void setAttributeCodeAllCache(Cache aAttributeCodeAllCache)
	{
		attributeCodeAllCache = aAttributeCodeAllCache;
	}

	public static Cache getComponentApprovalCache()
	{
		return componentApprovalCache;
	}

	public static Cache getApplicationCache()
	{
		return applicationCache;
	}

	public static Cache getComponentTypeCache()
	{
		return componentTypeCache;
	}

	public static Cache getContactCache()
	{
		return contactCache;
	}
	
	public static Cache getSearchCache()
	{
		return searchCache;
	}	

	@Override
	public void initialize()
	{
		OSFCacheManager.init();
		started.set(true);		
	}

	@Override
	public void shutdown()
	{
		OSFCacheManager.cleanUp();
		started.set(false);
	}

	@Override
	public boolean isStarted()
	{
		return started.get();
	}	
	
}
