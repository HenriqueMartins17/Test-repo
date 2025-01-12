package com.apitable.enterprise.apitablebilling.appsumo.annotation;

import com.apitable.enterprise.apitablebilling.appsumo.enums.AppsumoAction;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * mart appsumo event handler.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AppsumoEventHandler {
    /**
     * action type.
     *
     * @return AppsumoAction
     */
    AppsumoAction action();
}
