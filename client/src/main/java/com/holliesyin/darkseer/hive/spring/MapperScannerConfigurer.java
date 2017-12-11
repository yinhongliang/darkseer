package com.holliesyin.darkseer.hive.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class MapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {
    private String basePackage;
    private boolean addToConfig = true;
    private String hiveSessionFactoryBeanName;
    private String hiveSessionTemplateBeanName;
    private Class<? extends Annotation> annotationClass;
    private Class<?> markerInterface;
    private ApplicationContext applicationContext;
    private String beanName;
    private boolean processPropertyPlaceHolders;
    private BeanNameGenerator nameGenerator;

    public MapperScannerConfigurer() {
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> superClass) {
        this.markerInterface = superClass;
    }

    public void setHiveSessionFactoryBeanName(String hiveSessionFactoryName) {
        this.hiveSessionFactoryBeanName = hiveSessionFactoryName;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public BeanNameGenerator getNameGenerator() {
        return this.nameGenerator;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.basePackage, "Property \'basePackage\' is required");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if(this.processPropertyPlaceHolders) {
            this.processPropertyPlaceHolders();
        }

        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        scanner.setAddToConfig(this.addToConfig);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setHiveSessionFactoryBeanName(this.hiveSessionFactoryBeanName);
        scanner.setHiveSessionTemplateBeanName(this.hiveSessionTemplateBeanName);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ",; \t\n"));
    }

    private void processPropertyPlaceHolders() {
        Map prcs = this.applicationContext.getBeansOfType(PropertyResourceConfigurer.class);
        if(!prcs.isEmpty() && this.applicationContext instanceof GenericApplicationContext) {
            BeanDefinition mapperScannerBean = ((GenericApplicationContext)this.applicationContext).getBeanFactory().getBeanDefinition(this.beanName);
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(this.beanName, mapperScannerBean);
            Iterator values = prcs.values().iterator();

            while(values.hasNext()) {
                PropertyResourceConfigurer prc = (PropertyResourceConfigurer)values.next();
                prc.postProcessBeanFactory(factory);
            }

            MutablePropertyValues values1 = mapperScannerBean.getPropertyValues();
            this.basePackage = this.updatePropertyValue("basePackage", values1);
            this.hiveSessionFactoryBeanName = this.updatePropertyValue("hiveSessionFactoryBeanName", values1);
            this.hiveSessionTemplateBeanName = this.updatePropertyValue("hiveSessionTemplateBeanName", values1);
        }

    }

    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);
        if(property == null) {
            return null;
        } else {
            Object value = property.getValue();
            return value == null?null:(value instanceof String ?value.toString():(value instanceof TypedStringValue ?((TypedStringValue)value).getValue():null));
        }
    }
}