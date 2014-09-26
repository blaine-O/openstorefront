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
package edu.usu.sdl.openstorefront.sort;

import edu.usu.sdl.openstorefront.web.rest.model.AttributeCodeView;
import java.util.Comparator;

/**
 *
 * @author dshurtleff
 */
public class AttributeCodeViewComparator<T extends AttributeCodeView>
		implements Comparator<T>
{

	@Override
	public int compare(T o1, T o2)
	{
		return o1.getDescription().compareTo(o2.getDescription());
	}

}
