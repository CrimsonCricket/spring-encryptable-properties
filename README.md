# spring-encryptable-properties

Spring-encryptable-properties is a simple utility for enabling encryption of sensitive information within property
files, used for configuration of Spring applications. 

## License
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)



## Usage

### Extend ContextInitializer

The projects consists of a single ContextInitializer class. The class is abstract, and the idea is that you will extend
it in your project, and override some methods:

```java
import com.crimsoncricket.spring.encryptedproperties.ContextInitializer;

import java.util.ArrayList;
import java.util.List;

public class EncryptedPropertiesInitializer extends ContextInitializer {

	@Override
	protected String passwordEnvName() {
		return "EXAMPLE_APP_ENCRYPTION_KEY";
	}

	@Override
	protected List<String> propertySourceNames() {
		List<String> names= new ArrayList<>();
		names.add("application");
		return names;
	}

	@Override
	protected String overridesDirectory() {
		return "/etc/example-app";
	}
}
```

More specifically, you override the `passwordEnvName` method to specify the name of an environment variable. This
environment variable is then assumed to contain the encryption key to be used for decoding encrypted properties. Of 
course you will have to set the environment variable before starting your application. See for instance 
[This StackOverflow question](http://stackoverflow.com/questions/13980924/how-can-i-add-environment-variables-to-an-application-running-on-tomcat)
for hints how to do that in Apache Tomcat.

The `propertySourceNames` method must return a list of property file names (without the .properties extension) that
are to be loaded by the initializer. In the example above, the initializer is instructed to load the file 
`application.properties` from the root of the classpath


Also, optionally you can override the `overridesDirectory` method to specify the path of a directory that contains
property files containing overrides for the properties included in the classpath. This could be useful if you want
to want certain settings to be overridden on specific hosts or environments. 

### Encrypt your properties
The context initializer users [Jasypt](http://www.jasypt.org/) to decrypt your properties, using the algorithm 
`PBEWithMD5AndTripleDES`. The most convenient way to encrypt your properties before putting them in your configuration
files is to use the Jasypt command line API. To do this, you can simply download the latest distribution 
here: https://sourceforge.net/projects/jasypt/files/jasypt/
After downloading, just extract the archive to an arbitrary directory (e.g. ~/tools/jasypt), and make sure that the 
`encrypt.sh` file in the `bin` directory is executable.

Then, encrypt your value like this: 
```
./encrypt.sh input=some_encrypted_value password=kBJjt9GYxKAdzzCmThe0NFbUV algorithm=PBEWithMD5AndTripleDES
```
The expected output will be something like this:
```
----ENVIRONMENT-----------------

Runtime: Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 25.5-b02 



----ARGUMENTS-------------------

algorithm: PBEWithMD5AndTripleDES
input: some_encrypted_value
password: kBJjt9GYxKAdzzCmThe0NFbUV



----OUTPUT----------------------

3EMqKQSwjffT5uvsT2w32R/XjzUXnn1r+hjeL5XltNc=
```
 
The output can be used as an encrypted property value, if you surround it with `ENC()`. So, in the above example,
your property file could look like this:
```properties
some.encrypted.property=ENC(3EMqKQSwjffT5uvsT2w32R/XjzUXnn1r+hjeL5XltNc=)
some.other.encrypted.property=ENC(u3gtEyPmetH7Cuxjy3bgCPH/LUcpdNd1pTHTnMzllDJynupIQ1tKqw==)
some.unencrypted.property=some_plaintext_value
```


### Register the ContextInitializer
In order for the encrypted properties to be accessible in your configuration files, the ContextInitializer must be 
registered to initialize the application context, before the Spring application starts its regular initialization. In
a web application using the Spring `DispatcherServlet`, you can register the initializer in you `WEB-INF/web.xml` file:

```xml
	<context-param>
		<param-name>contextInitializerClasses</param-name>
		<param-value>com.crimsoncricket.example.encryptedproperties.port.adapter.spring.context.EncryptedPropertiesInitializer</param-value>
	</context-param>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>
```


### Decrypt and use the properties in your Spring application
The context initializer uses the Spring integration facilities in Jasypt to allow using encrypted properties in your
Spring application in the same manner as you would use regular, unencrypted property sources. Some examples:


#### In a `@Configuration` bean:
```java
import com.crimsoncricket.example.encryptedproperties.application.SomeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import javax.annotation.Resource;

@Configuration
public class ApplicationConfig  {
	
	@Resource
	private Environment environment;

	@Bean
	public SomeService someService() {
		return new SomeService(environment.getRequiredProperty("some.encrypted.property"));
	}
	
}
```

### In an autowired `@Component`:
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SomeOtherService {

	private final String someOtherEncryptedProperty;

	@Autowired
	public SomeOtherService(
			@Value("${some.other.encrypted.property}") String someOtherEncryptedProperty
	) {
		this.someOtherEncryptedProperty = someOtherEncryptedProperty;
	}

}
```

## Example
For your convenience, a very minimal web application using encrypted properties is included in the /example directory of
this repository. 














