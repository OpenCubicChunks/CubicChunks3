package io.github.opencubicchunks.cubicchunks.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Tag("longRunTest")
public @interface LongRunTest {}
