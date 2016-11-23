package io.ghostwriter.rt.snaperr.serializer;


import io.ghostwriter.rt.snaperr.tracker.ReferenceTracker;
import io.ghostwriter.rt.snaperr.tracker.TrackedScope;
import io.ghostwriter.rt.snaperr.tracker.TrackedValue;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;
import io.ghostwriter.rt.snaperr.trigger.TimeoutTrigger;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class StringSerializer implements TriggerSerializer<String> {

    @Override
    public String serializeTrigger(ErrorTrigger errorTrigger) {
        Objects.requireNonNull(errorTrigger);
        ReferenceTracker referenceTracker = errorTrigger.getReferenceTracker();
        // FIXME(snorbi07): needs to be extended to support scope iteration
        TrackedScope topLevelScope = referenceTracker.currentScope();

        final Object source = topLevelScope.getSource();
        final String methodName = topLevelScope.getMethodName();

        String contextCanonicalName = source.getClass().getCanonicalName();
        if (source instanceof Class<?>) {
            Class<?> klazz = (Class<?>) source;
            contextCanonicalName = klazz.getCanonicalName();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(contextCanonicalName).append(".").append(methodName).append(" {\n");

        final Map<String, TrackedValue> watched = topLevelScope.getReferences();
        appendWatchedValues(sb, watched);

        sb.append("}");

        return sb.toString();
    }

    @Override
    public String serializeTrigger(TimeoutTrigger timeoutTrigger) {
        throw new UnsupportedOperationException("missing implementation");
    }

    private void appendWatchedValues(StringBuilder sb, Map<String, TrackedValue> watched) {
        for (TrackedValue trackedValue : watched.values()) {
            final String name = trackedValue.getName();
            final Object value = trackedValue.getValue();

            String strValue = valueToString(value);
            sb.append("\t").append(name).append("=").append(strValue).append("\n");
        }
    }

    private String valueToString(Object value) {
        String stringValue = String.valueOf(value);
        if (!isToStringProvided(value)) {
            stringValue = ReflectionToStringBuilder.toString(value);
        }

        return stringValue;
    }

    private boolean isToStringProvided(Object value) {
        if (value == null) {
            return true; // String.valueOf will handle null properly
        }

        final Class<?> aClass = value.getClass();
        final String toStringMethodName = "toString";
        try {
            final Method toString = aClass.getMethod(toStringMethodName);
            final Class<?> declaringClass = toString.getDeclaringClass();

            return declaringClass != Object.class;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + aClass.getName() + " does not provide method: " + toStringMethodName + ". Error: " + e);
        }

    }

}
