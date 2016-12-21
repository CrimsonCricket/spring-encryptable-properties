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

package com.crimsoncricket.example.encryptedproperties.port.adapter.spring.mvc;

import com.crimsoncricket.example.encryptedproperties.application.SomeOtherService;
import com.crimsoncricket.example.encryptedproperties.application.SomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(path = "/")
public class SomeController {


	private final SomeService someService;
	private final SomeOtherService someOtherService;


	@Autowired
	public SomeController(
			SomeService someService,
			SomeOtherService someOtherService
	) {
		this.someService = someService;
		this.someOtherService = someOtherService;
	}

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public ModelAndView  somePage() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("some_encrypted_property", someService.getSomeEncryptedProperty());
		modelAndView.addObject("some_other_encrypted_property", someOtherService.getSomeOtherEncryptedProperty());
		modelAndView.setViewName("index");
		return modelAndView;
	}


}
