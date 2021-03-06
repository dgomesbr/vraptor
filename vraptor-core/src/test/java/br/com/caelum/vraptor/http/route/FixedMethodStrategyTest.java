/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.caelum.vraptor.http.route;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.http.MutableRequest;
import br.com.caelum.vraptor.interceptor.VRaptorMatchers;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;
import br.com.caelum.vraptor.resource.HttpMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.test.VRaptorMockery;

public class FixedMethodStrategyTest {

	private VRaptorMockery mockery;
	private MutableRequest request;
	private ParametersControl control;

	@Before
	public void setup() {
		this.mockery = new VRaptorMockery();
		this.request = mockery.mock(MutableRequest.class);
		this.control = mockery.mock(ParametersControl.class);
	}

	@Test
	public void canTranslate() {
		FixedMethodStrategy strategy = new FixedMethodStrategy("abc", DefaultResourceMethod.instanceFor(MyControl.class, method("list")),
				methods(HttpMethod.POST), control, 0, new String[] {});
		mockery.checking(new Expectations() {
			{
				one(control).matches("/clients/add"); will(returnValue(true));
				one(control).fillIntoRequest("/clients/add", request);
			}
		});
		ResourceMethod match = strategy.resourceMethod(request, "/clients/add");
		assertThat(match, is(VRaptorMatchers.resourceMethod(method("list"))));
	}

	@SuppressWarnings("unchecked")
	private Set<HttpMethod> methods(HttpMethod method) {
		return new HashSet(Arrays.asList(method));
	}

	@SuppressWarnings("unchecked")
	private Method method(String name, Class... types) {
		try {
			return MyControl.class.getDeclaredMethod(name, types);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	class Dog {
		private Long id;

		public void setId(Long id) {
			this.id = id;
		}

		public Long getId() {
			return id;
		}
	}

	@br.com.caelum.vraptor.Resource
	public static class MyControl {
		public void add(Dog object) {
		}

		public void unknownMethod() {
		}

		public void list() {
		}

		public void show(Dog dog) {
		}
	}

}
