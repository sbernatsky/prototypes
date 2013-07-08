package proto.action.spring;

import java.lang.reflect.Method;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ReflectionUtils;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionParameter;
import proto.action.annotation.ValueConstants;


class HandlerMethod {
    private Object bean;
    private Method method;
    //private Method bridgedMethod;
    private MethodParameter<?>[] parameters;
    private Converter<Object, ActionResult> resultConverter;

    public ActionResult invoke(Action action) {
        try {
            Object[] params = createInvocationParameters(action);
            Object methodResult = method.invoke(bean, params);
            return resultConverter.convert(methodResult);
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
            return null;
        }
    }

    Object[] createInvocationParameters(Action action) {
        Object[] result = new Object[parameters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = parameters[i].getValue(action);
        }
        return result;
    }

    public Object getBean() {
        return bean;
    }

    public static abstract interface MethodParameter<T> {
        T getValue(Action action);
    }

    public static class ActionMethodParameter implements MethodParameter<Action> {

        @Override
        public Action getValue(Action action) {
            return action;
        }
    }

    public static class ConvertibleMethodParameter<T> implements MethodParameter<T> {
        private Converter<String, T> converter;
        private ActionParameter desc;

        @Override
        public T getValue(Action action) {
            String value = action.getParameterValue(desc.value());

            if (desc.required() && value == null) {
                throw new IllegalArgumentException("missing required parameter: "+ desc.value());
            }

            if (value == null && desc.defaultValue() != ValueConstants.DEFAULT_NONE) {
                value = desc.defaultValue();
            }

            return converter.convert(value);
        }
    }

}

