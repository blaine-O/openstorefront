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
package edu.usu.sdl.openstorefront.core.sort;

import edu.usu.sdl.openstorefront.core.view.AttributeCodeView;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author dshurtleff
 * @param <T>
 */
public class AttributeCodeViewComparator<T extends AttributeCodeView>
		implements Comparator<T>, Serializable
{

	@Override
	public int compare(T o1, T o2)
	{
		if (o1.getSortOrder() != null && o2.getSortOrder() != null) {
			return o1.getSortOrder().compareTo(o2.getSortOrder());
		} else {
			return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
		}
	}

}
