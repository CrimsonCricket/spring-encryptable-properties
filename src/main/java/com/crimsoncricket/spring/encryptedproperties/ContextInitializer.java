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

package com.crimsoncricket.spring.encryptedproperties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.spring31.properties.EncryptablePropertiesPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;


/**
 * Initializer that configures Jasypt-based encrypted property sources to be used
 * within Spring applications.
 *
 * To use the initializer, extend this class in you own project.
 *
 */
public abstract class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger logger = LoggerFactory.getLogger(ContextInitializer.class);
	private final StandardPBEStringEncryptor encryptor;

	/**
	 * Default constructor. Usually, in web applications,  the constructor will be called by a
	 * {@link org.springframework.web.context.ContextLoaderListener}
	 */
	public ContextInitializer() {

		EnvironmentStringPBEConfig encryptionConfig = new EnvironmentStringPBEConfig();
		encryptionConfig.setAlgorithm("PBEWithMD5AndTripleDES");
		encryptionConfig.setPasswordEnvName(passwordEnvName());

		encryptor = new StandardPBEStringEncryptor();
		encryptor.setConfig(encryptionConfig);
	}

	/**
	 * Override this method to provide the initializer with the key to use for decrypting encrypted properties
	 * @return the name of the environment variable that contains the decryption key for the property sources
	 */
	@SuppressWarnings("WeakerAccess")
	protected abstract String passwordEnvName();


	/**
	 *  Usually, in web applications,  the intialize method will be called by a
	 * {@link org.springframework.web.context.ContextLoaderListener}
	 *
	 * This method configures property sources for all source names provided by
	 * the overridden implementation of {@link #propertySourceNames}
	 *
	 * @param applicationContext a configurable Spring application context
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		List<String> propertySourceNames = propertySourceNames();
		for (String name : propertySourceNames)
			addPropertySource(applicationContext, name);
	}

	/**
	 * Override this method to specify the names of the encrypted property files
	 * to be loaded by this initializer. Provide a list of names WITHOUT the .properties extension.
	 * @return a list of property file names, without the .properties extension
	 */
	@SuppressWarnings("WeakerAccess")
	protected abstract List<String> propertySourceNames();


	private void addPropertySource(ConfigurableApplicationContext context, String name) {
		addPropertySourceFromClasspath(context, name);
		addPropertyOverridesFromFileSystem(context, name);
	}


	private void addPropertySourceFromClasspath(ConfigurableApplicationContext context, String name) {
		Resource resource = resourceFromClassPath(name);
		Properties properties = loadedPropertiesWithRuntimeExceptionOnFailure(resource);
		addEncryptablePropertiesToContext(context, name, properties);
	}

	private Resource resourceFromClassPath(String name) {
		return new ClassPathResource("/" + name + ".properties");
	}

	private Properties loadedPropertiesWithRuntimeExceptionOnFailure(Resource resource) {
		try {
			return PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addPropertyOverridesFromFileSystem(ConfigurableApplicationContext context, String name) {
		Resource resource = resourceFromFileSystem(name);
		Optional<Properties> properties = loadedPropertiesWithEmptyValueOnFailure(resource);
		properties.ifPresent(props -> addEncryptablePropertiesToContext(context, name + "Override", props));
	}

	private Resource resourceFromFileSystem(String name) {
		return new FileSystemResource(overridesDirectory() + "/" + name + ".properties");
	}

	/**
	 * Override this method to specify a directory containing overrides for the proeprty files
	 * loaded by this initializer. For each name specified in {@link #propertySourceNames()},
	 * the initializer will try to find an equally named file in this directory.
	 *
	 * If the override file does not exist, it will be ignored.
	 * If the override file does exist, all properties in that file will override the value
	 * of an equally named property specified in the original file.
	 *
	 * @return the full directory path of a directory containing overrides for property files
	 */
	@SuppressWarnings("WeakerAccess")
	protected String overridesDirectory() {
		return "";
	}

	private Optional<Properties> loadedPropertiesWithEmptyValueOnFailure(Resource resource) {
		try {
			return Optional.of(PropertiesLoaderUtils.loadProperties(resource));
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private void addEncryptablePropertiesToContext(ConfigurableApplicationContext context, String name, Properties properties) {
		EncryptablePropertiesPropertySource propertySource = encryptablePropertySource(name, properties);
		addPropertySourceToContext(context, propertySource);
		logger.info("Encryptable properties added: " + name);
	}

	private EncryptablePropertiesPropertySource encryptablePropertySource(String name, Properties properties) {
		return new EncryptablePropertiesPropertySource(
				name,
				properties,
				encryptor
		);
	}

	private void addPropertySourceToContext(ConfigurableApplicationContext context, EncryptablePropertiesPropertySource propertySource) {
		context.getEnvironment().getPropertySources().addFirst(propertySource);
	}


}

