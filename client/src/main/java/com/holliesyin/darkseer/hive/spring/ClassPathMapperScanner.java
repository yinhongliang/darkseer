package com.holliesyin.darkseer.hive.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class ClassPathMapperScanner extends ClassPathBeanDefinitionScanner {
    private boolean addToConfig = true;
    private String hiveSessionTemplateBeanName;
    private String hiveSessionFactoryBeanName;
    private Class<? extends Annotation> annotationClass;
    private Class<?> markerInterface;

    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public void setHiveSessionTemplateBeanName(String hiveSessionTemplateBeanName) {
        this.hiveSessionTemplateBeanName = hiveSessionTemplateBeanName;
    }

    public void setHiveSessionFactoryBeanName(String hiveSessionFactoryBeanName) {
        this.hiveSessionFactoryBeanName = hiveSessionFactoryBeanName;
    }

    public void registerFilters() {
        boolean acceptAllInterfaces = true;
        if(this.annotationClass != null) {
            this.addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
            acceptAllInterfaces = false;
        }

        if(this.markerInterface != null) {
            this.addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if(acceptAllInterfaces) {
            this.addIncludeFilter(new TypeFilter() {
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                    return true;
                }
            });
        }

        this.addExcludeFilter(new TypeFilter() {
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set beanDefinitions = super.doScan(basePackages);
        if(beanDefinitions.isEmpty()) {
            this.logger.warn("No MyBatis mapper was found in \'" + Arrays.toString(basePackages) + "\' package. Please check your configuration.");
        } else {
            Iterator i$ = beanDefinitions.iterator();

            while(i$.hasNext()) {
                BeanDefinitionHolder holder = (BeanDefinitionHolder)i$.next();
                GenericBeanDefinition definition = (GenericBeanDefinition)holder.getBeanDefinition();
                if(this.logger.isDebugEnabled()) {
                    this.logger.debug("Creating MapperFactoryBean with name \'" + holder.getBeanName() + "\' and \'" + definition.getBeanClassName() + "\' mapperInterface");
                }

                definition.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
                definition.setBeanClass(MapperFactoryBean.class);
                definition.getPropertyValues().add("addToConfig", Boolean.valueOf(this.addToConfig));
                boolean explicitFactoryUsed = false;
                if(StringUtils.hasText(this.hiveSessionFactoryBeanName)) {
                    definition.getPropertyValues().add("hiveSessionFactory", new RuntimeBeanReference(this.hiveSessionFactoryBeanName));
                    explicitFactoryUsed = true;
                }

                if(StringUtils.hasText(this.hiveSessionTemplateBeanName)) {
                    if(explicitFactoryUsed) {
                        this.logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
                    }

                    definition.getPropertyValues().add("hiveSessionTemplate", new RuntimeBeanReference(this.hiveSessionTemplateBeanName));
                    explicitFactoryUsed = true;
                }

                if(!explicitFactoryUsed) {
                    if(this.logger.isDebugEnabled()) {
                        this.logger.debug("Enabling autowire by type for MapperFactoryBean with name \'" + holder.getBeanName() + "\'.");
                    }

                    definition.setAutowireMode(2);
                }
            }
        }

        return beanDefinitions;
    }

    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        if(super.checkCandidate(beanName, beanDefinition)) {
            return true;
        } else {
            this.logger.warn("Skipping MapperFactoryBean with name \'" + beanName + "\' and \'" + beanDefinition.getBeanClassName() + "\' mapperInterface" + ". Bean already defined with the same name!");
            return false;
        }
    }
}
