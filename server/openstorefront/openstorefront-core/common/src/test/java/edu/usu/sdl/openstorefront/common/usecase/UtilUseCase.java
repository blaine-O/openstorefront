/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.common.usecase;

import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.junit.Test;

/**
 *
 * @author dshurtleff
 */
public class UtilUseCase
{
	
	@Test
	public void testConverting()
	{
		
		System.out.println("0029 = " + Convert.toInteger("0029"));
		
		Charset utf8charset = Charset.forName("UTF-8");
		ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[]{(byte)29});
		CharBuffer data = utf8charset.decode(inputBuffer);
		
		
		System.out.println("0029 = " + ((char)33));
		
		System.out.println(StringProcessor.decodeHexCharEscapes("x0029"));
		System.out.println(StringProcessor.decodeHexCharEscapes("x0020"));
		System.out.println(StringProcessor.decodeHexCharEscapes("x0028"));
		System.out.println(StringProcessor.decodeHexCharEscapes("x0065"));
		
	}
	
}
