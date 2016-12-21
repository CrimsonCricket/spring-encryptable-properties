/*
 * Copyright 2016 Martijn van der Woud - The Crimson Cricket Internet Services
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.crimsoncricket.example.encryptedproperties.port.adapter.spring.context;

import com.crimsoncricket.spring.encryptedproperties.ContextInitializer;

import java.util.ArrayList;
import java.util.List;

public class EncryptedPropertiesInitializer extends ContextInitializer {

	protected String passwordEnvName() {
		return "EXAMPLE_APP_ENCRYPTION_KEY";
	}

	protected List<String> propertySourceNames() {
		List<String> names= new ArrayList<>();
		names.add("application");
		return names;
	}

	protected String overridesDirectory() {
		return "/etc/example-app";
	}
}
