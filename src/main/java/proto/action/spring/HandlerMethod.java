package proto.action.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionParameter;
import proto.action.annotation.ValueConstants;


class HandlerMethod {
    private static final TypeDescriptor returnTarget = TypeDescriptor.valueOf(ActionResult.class);
    private static final TypeDescriptor paramSource = TypeDescriptor.valueOf(String.class);
    private static final MethodParameter actionMethodParameter = new MethodParameter() {

        @Override
        public Action getValue(Action action) {
            return action;
        }
    };


    private final Object bean;
    private final Method method;
    private final TypeDescriptor returnSource;
    private final MethodParameter[] parameters;
    private final ConversionService conversionService;

    public HandlerMethod(Object handler, Method method, ConversionService conversionService) {
        this.bean = handler;
        this.method = method;
        this.conversionService = conversionService;

        this.returnSource = TypeDescriptor.valueOf(method.getReturnType());
        if (!conversionService.canConvert(returnSource, returnTarget)) {
            throw new RuntimeException(String.format("can not convert result %s -> %s", returnSource.getType(), returnTarget.getType()));
        }

        this.parameters = new MethodParameter[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (Action.class.isAssignableFrom(parameterType)) {
                this.parameters[i] = actionMethodParameter;
            } else {
                TypeDescriptor paramTarget = TypeDescriptor.valueOf(parameterType);
                if (!conversionService.canConvert(paramSource, paramTarget)) {
                    throw new RuntimeException(String.format("can not convert parameter %s -> %s", returnSource.getType(), returnTarget.getType()));
                }
                ActionParameter desc = null;
                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                    if (ActionParameter.class.isInstance(annotation)) {
                        desc = ActionParameter.class.cast(annotation);
                        break;
                    }
                }
                if (desc == null) {
                    throw new RuntimeException(String.format("missing @%s annotation on %s'th parameter", ActionParameter.class, i));
                }
                this.parameters[i] = new ConvertibleMethodParameter(paramTarget, desc);
            }
        }
    }

    public ActionResult invoke(Action action) {
        try {
            Object[] params = createInvocationParameters(action);
            Object methodResult = method.invoke(bean, params);
            return (ActionResult) conversionService.convert(methodResult, paramSource, returnTarget);
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

    public static abstract interface MethodParameter {
        Object getValue(Action action);
    }

    public class ConvertibleMethodParameter implements MethodParameter {
        private final TypeDescriptor targetType;
        private final ActionParameter desc;

        public ConvertibleMethodParameter(TypeDescriptor targetType, ActionParameter desc) {
            this.targetType = targetType;
            this.desc = desc;
        }

        @Override
        public Object getValue(Action action) {
            String value = action.getParameterValue(desc.value());

            if (desc.required() && value == null) {
                throw new IllegalArgumentException("missing required parameter: "+ desc.value());
            }

            if (value == null && desc.defaultValue() != ValueConstants.DEFAULT_NONE) {
                value = desc.defaultValue();
            }

            return conversionService.convert(value, paramSource, targetType);
        }
    }

}

