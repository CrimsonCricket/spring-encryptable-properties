/*
 * Copyright 2015 Martijn van der Woud - The Crimson Cricket Internet Services
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



public abstract class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(ContextInitializer.class);
    private final StandardPBEStringEncryptor encryptor;

    public ContextInitializer() {

        EnvironmentStringPBEConfig encryptionConfig = new EnvironmentStringPBEConfig();
        encryptionConfig.setAlgorithm("PBEWithMD5AndTripleDES");
        encryptionConfig.setPasswordEnvName(passwordEnvName());

        encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(encryptionConfig);
    }

    protected abstract String passwordEnvName();

    public void initialize(ConfigurableApplicationContext applicationContext) {
        List<String> propertySourceNames = propertySourceNames();
        for (String name : propertySourceNames)
            addPropertySource(applicationContext, name);
    }

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
        Optional<Properties> properties = loadedPropertiesWithEmtpyValueOnFailure(resource);
        if (properties.isPresent())
            addEncryptablePropertiesToContext(context, name + "Override", properties.get());
    }

    private Resource resourceFromFileSystem(String name) {
        return new FileSystemResource("/etc/professionsearch/" + name + ".properties");
    }

    private Optional<Properties> loadedPropertiesWithEmtpyValueOnFailure(Resource resource) {
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

