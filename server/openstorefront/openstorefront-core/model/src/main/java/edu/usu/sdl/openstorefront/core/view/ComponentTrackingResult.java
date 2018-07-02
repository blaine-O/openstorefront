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
package edu.usu.sdl.openstorefront.core.view;

import edu.usu.sdl.openstorefront.core.annotation.DataType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jlaw
 */
public class ComponentTrackingResult
{

	@DataType(ComponentTrackingCompleteWrapper.class)
	private List<ComponentTrackingCompleteWrapper> result = new ArrayList<>();
	private long count;

	public List<ComponentTrackingCompleteWrapper> getResult()
	{
		return result;
	}

	public void setResult(List<ComponentTrackingCompleteWrapper> result)
	{
		this.result = result;
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

}
