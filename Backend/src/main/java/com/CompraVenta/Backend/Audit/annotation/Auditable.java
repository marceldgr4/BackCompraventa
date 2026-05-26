package com.CompraVenta.Backend.Audit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String operation();
    String entity() default "";
}
