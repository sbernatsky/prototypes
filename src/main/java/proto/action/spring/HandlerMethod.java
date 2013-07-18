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
    private static final MethodParameterConverter actionMethodParameter = new MethodParameterConverter() {
        @Override public Action getValue(Action action) { return action; }
    };
    private static final ResultConverter<Void> VOID_RESULT_CONVERTER = new ResultConverter<Void>() {
        @Override public ActionResult convert(Void result) { return ActionResult.SUCCESS; }
    };
    private static final ResultConverter<ActionResult> ACTION_RESULT_CONVERTER = new ResultConverter<ActionResult>() {
        @Override public ActionResult convert(ActionResult result) { return result; }
    };

    private final Object bean;
    private final Method method;
    private final ResultConverter<? extends Object> resultConverter;
    private final MethodParameterConverter[] parameters;
    private final ConversionService conversionService;

    public HandlerMethod(Object handler, Method method, ConversionService conversionService) {
        this.bean = handler;
        this.method = method;
        this.conversionService = conversionService;

        Class<?> returnType = method.getReturnType();
        if (Void.class.isAssignableFrom(returnType)
                || Void.TYPE.isAssignableFrom(returnType)) {
            this.resultConverter = VOID_RESULT_CONVERTER;
        } else if (ActionResult.class.isAssignableFrom(returnType)) {
            this.resultConverter = ACTION_RESULT_CONVERTER;
        } else {
            TypeDescriptor returnSource = TypeDescriptor.valueOf(method.getReturnType());
            if (!conversionService.canConvert(returnSource, returnTarget)) {
                throw new RuntimeException(String.format("can not convert result %s -> %s for method %s",
                                           returnSource.getType(), returnTarget.getType(), method));
            }
            this.resultConverter = new ConvertibleResultConverter<Object>(returnSource);
        }

        this.parameters = new MethodParameterConverter[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (Action.class.isAssignableFrom(parameterType)) {
                this.parameters[i] = actionMethodParameter;
            } else {
                TypeDescriptor paramTarget = TypeDescriptor.valueOf(parameterType);
                if (!conversionService.canConvert(paramSource, paramTarget)) {
                    throw new RuntimeException(String.format("can not convert parameter %s -> %s for method %s",
                                               paramSource.getType(), paramTarget.getType(), method));
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

    public static interface MethodParameterConverter {
        Object getValue(Action action);
    }

    public class ConvertibleMethodParameter implements MethodParameterConverter {
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

            if (value == null && !ValueConstants.DEFAULT_NONE.equals(desc.defaultValue())) {
                value = desc.defaultValue();
            }

            return conversionService.convert(value, paramSource, targetType);
        }
    }

    public static interface ResultConverter<T> {
        ActionResult convert(T result);
    }

    public class ConvertibleResultConverter<T> implements ResultConverter<T> {
        private final TypeDescriptor sourceType;

        public ConvertibleResultConverter(TypeDescriptor sourceType) {
            this.sourceType = sourceType;
        }

        @Override
        public ActionResult convert(T result) {
            return (ActionResult) conversionService.convert(result, sourceType, returnTarget);
        }
    }

}

