package proto.action.spring.converters;

import org.springframework.core.convert.converter.Converter;
import proto.action.ActionResult;

public class IntegerToActionResultConverter implements Converter<Integer, ActionResult> {

    @Override
    public ActionResult convert(Integer s) {
        return new ActionResult(s.intValue());
    }

}
