package proto.action.spring;

import java.lang.reflect.Method;

import org.springframework.core.convert.ConversionService;
import org.springframework.util.ReflectionUtils;
import proto.action.Action;
import proto.action.ActionResult;


class HandlerMethod {
    private final Object bean;
    private final Method method;
    private final ResultConverter<? extends Object> resultConverter;
    private final MethodParameterConverter[] parameters;

    public HandlerMethod(Object handler, Method method, ConversionService conversionService) {
        this.bean = handler;
        this.method = method;

        this.resultConverter = HandlerMethodUtils.createResultConverter(method, conversionService);
        this.parameters = HandlerMethodUtils.createMethodParameterConverters(method, conversionService);
    }

    @SuppressWarnings("unchecked")
    public ActionResult invoke(Action action) {
        try {
            Object[] params = createInvocationParameters(action);
            Object methodResult = method.invoke(bean, params);
            return ((ResultConverter<Object>)resultConverter).convert(methodResult);
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
            return null;
        }
    }

    private Object[] createInvocationParameters(Action action) {
        Object[] result = new Object[parameters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = parameters[i].getValue(action);
        }
        return result;
    }

    Object getBean() {
        return bean;
    }

    @Override
    public int hashCode() {
        return bean.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod another = (HandlerMethod) obj;
        return this.bean.equals(another.bean) && this.method.equals(another.method);
    }

    @Override
    public String toString() {
        return String.format("%s -> (%s)", bean.getClass(), method);
    }

    public static interface MethodParameterConverter {
        Object getValue(Action action);
    }

    public static interface ResultConverter<T> {
        ActionResult convert(T result);
    }

}

