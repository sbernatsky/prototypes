package proto.action.spring;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import proto.action.ActionHandler;
import proto.action.ActionHandlerRegistry;

public class SpringActionHandlerRegistry implements ActionHandlerRegistry, BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // TODO Auto-generated method stub
        Void annotation = AnnotationUtils.findAnnotation(bean.getClass(), Void.class);
        if (annotation != null) {
        }
        return bean;
    }

    @Override
    public ActionHandler lookup(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLegacyHandlers(Map<String, ActionHandler> handlers) {
        // TODO Auto-generated method stub
    }
}
