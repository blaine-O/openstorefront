/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.core.api;

import edu.usu.sdl.openstorefront.core.entity.ChangeLog;
import edu.usu.sdl.openstorefront.core.entity.LoggableModel;
import edu.usu.sdl.openstorefront.core.entity.StandardEntity;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public interface ChangeLogService
		extends AsyncService
{

	/**
	 * Finds changes and saves them
	 *
	 * @param <T>
	 * @param original
	 * @param updated
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity & LoggableModel> List<ChangeLog> findUpdateChanges(T original, T updated);

	/**
	 * Find changes (optional saves changes) between the original and the
	 * updated entity
	 *
	 * @param <T>
	 * @param original
	 * @param updated
	 * @param save
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity & LoggableModel> List<ChangeLog> findUpdateChanges(T original, T updated, boolean save);

	/**
	 * Save a added change record to parent entities history
	 *
	 * @param <T>
	 * @param parentEntity
	 * @param addedEntity
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity> ChangeLog addEntityChange(T addedEntity);

	/**
	 * Create a record for a remove entity (Sub-entity)
	 *
	 * @param <T>
	 * @param parentEntity
	 * @param temovedEntity
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity> ChangeLog removeEntityChange(T removedEntity);

	/**
	 * This is used for the records deleting a set of (sub-entities)
	 *
	 * @param <T>
	 * @param parentEntity
	 * @param temovedEntity
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity> ChangeLog removedAllEntityChange(T exampleRemovedEntity);

	/**
	 * Logs and active status change in cases where that not handle in an
	 * update.
	 *
	 * @param <T>
	 * @param parentEntity
	 * @param statusEntity
	 * @param newStatus
	 * @return
	 */
	@ServiceInterceptor(TransactionInterceptor.class)
	public <T extends StandardEntity> ChangeLog logStatusChange(final T statusEntity, String newStatus);

}
