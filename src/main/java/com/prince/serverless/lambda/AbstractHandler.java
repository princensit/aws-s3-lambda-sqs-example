package com.prince.serverless.lambda;

import java.lang.reflect.ParameterizedType;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * This abstract class configures Spring IoC automatically. To enable this support, concrete handler
 * classes extends this class with T type which should implement @Configuration interface.
 *
 * @param <T> T class should implement @Configuration interface
 * @see MainHandler
 * @author Prince Raj
 */
@SuppressWarnings("unused")
abstract class AbstractHandler<T> {

    // Spring IoC application context
    private ApplicationContext applicationContext;

    AbstractHandler() {
        // Gets config class to create an Application context
        Class typeParameterClass = ((Class) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]);

        // Check if T has @Configuration annotation, if no throws an exception
        if (!typeParameterClass.isAnnotationPresent(Configuration.class)) {
            throw new RuntimeException(typeParameterClass + " is not a @Configuration class");
        }

        // Create Spring application context
        applicationContext = new AnnotationConfigApplicationContext(typeParameterClass);
    }

    /**
     * Use this getter to access to Spring Application Context
     *
     * @return ApplicationContext
     */
    ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
