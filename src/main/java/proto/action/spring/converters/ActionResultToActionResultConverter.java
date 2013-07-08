package proto.action.spring.converters;

import org.springframework.core.convert.converter.Converter;
import proto.action.ActionResult;

public class ActionResultToActionResultConverter implements Converter<ActionResult, ActionResult> {

    @Override
    public ActionResult convert(ActionResult s) {
        return s;
    }

}
