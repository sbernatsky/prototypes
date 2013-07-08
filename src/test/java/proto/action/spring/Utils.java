package proto.action.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
import proto.action.Action;
import proto.action.ActionResult;
import proto.action.annotation.ActionMethod;
import proto.action.spring.converters.ActionResultToActionResultConverter;
import proto.action.spring.converters.IntegerToActionResultConverter;
import proto.action.spring.converters.VoidToActionResultConverter;


public class Utils {
    private final Map<Class<?>, Converter<?, ActionResult>> returnTypeConverters;

    {
        returnTypeConverters = new HashMap<Class<?>, Converter<?,ActionResult>>();
        returnTypeConverters.put(Void.class, new VoidToActionResultConverter());
        returnTypeConverters.put(ActionResult.class, new ActionResultToActionResultConverter());
        returnTypeConverters.put(int.class, new IntegerToActionResultConverter());
        returnTypeConverters.put(Integer.class, new IntegerToActionResultConverter());
    }

    public static void test() {
        new Utils().run(new SampleActionHandler());
        Converter<?,ActionResult> result = new VoidToActionResultConverter();
        
    }

    private void run(Object handler) {
        
        // TODO Auto-generated method stub
        
    }

    public static class SampleActionHandler {
        @ActionMethod("void")
        public void voidMethod(Action action) {
            Assert.notNull(action);
        }
    }
}
