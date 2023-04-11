package com.chbcraft.internals.components.listen;

import com.chbcraft.internals.components.enums.EventPriority;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    EventPriority value() default EventPriority.MIDDLE;
}
