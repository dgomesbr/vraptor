/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor.ioc.pico;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.caelum.vraptor.resource.DefaultResource;
import br.com.caelum.vraptor.resource.Resource;

public class DefaultDirScanner implements DirScanner {

	public List<Resource> scan(File dir) {
		List<Resource> resources = new ArrayList<Resource>();
		search(dir, "", resources);
		return resources;
	}

	private void search(File baseDir, String currentPackage,
			List<Resource> resources) {
		if (baseDir.listFiles() == null) {
			return;
		}
		if (!currentPackage.equals("")) {
			currentPackage += ".";
		}
		for (File child : baseDir.listFiles()) {
			if (child.isHidden()) {
				continue;
			}
			if (child.isDirectory()) {
				search(child, currentPackage + child.getName(), resources);
			} else if (child.isFile() && child.getName().endsWith(".class")) {
				String typeName = currentPackage + child.getName();
				typeName = typeName.substring(0, typeName.length()-6);
				try {
					Class<?> type = Class.forName(typeName, false, this.getClass()
							.getClassLoader());
					if (type.isAnnotationPresent(br.com.caelum.vraptor.Resource.class)) {
						resources.add(new DefaultResource(type));
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
