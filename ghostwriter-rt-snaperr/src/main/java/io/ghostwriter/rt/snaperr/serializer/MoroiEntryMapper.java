package io.ghostwriter.rt.snaperr.serializer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.ghostwriter.rt.snaperr.moroi.Attributes;
import io.ghostwriter.rt.snaperr.moroi.Context;
import io.ghostwriter.rt.snaperr.moroi.ContextData;
import io.ghostwriter.rt.snaperr.moroi.MoroiEntry;
import io.ghostwriter.rt.snaperr.moroi.MoroiEntryData;
import io.ghostwriter.rt.snaperr.moroi.Variable;
import io.ghostwriter.rt.snaperr.tracker.TrackedScope;
import io.ghostwriter.rt.snaperr.tracker.TrackedValue;
import io.ghostwriter.rt.snaperr.trigger.ErrorTrigger;

public class MoroiEntryMapper {

    private static final String MOROI_MESSAGE_TYPE_EXCEPTION = "exception";
    final private String applicationUUID;

    public MoroiEntryMapper(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public MoroiEntry toMoroiEntry(ErrorTrigger errorTrigger) {
        Objects.requireNonNull(errorTrigger);

        // FIXME(snorbi07): this needs to be extended to support multiple scopes
        TrackedScope topLevelScope = errorTrigger.currentScope();

        Object source = topLevelScope.getSource();

        final Throwable throwable = errorTrigger.getThrowable();

        Attributes representation = new Attributes();
        representation.setApplicationUUID(applicationUUID);
        representation.setException(getExceptionName(throwable));
        representation.setStackTrace(stackTraceToString(throwable));
        representation.setSourceOfError(getSourceName(source));
        representation.setTimestamp(getMoroiTimestamp());

        final StackTraceElement[] stackTraceElements = limitedStackTrace(throwable, 1);
        if (stackTraceElements.length > 0) {
            final StackTraceElement rootStackTraceElement = stackTraceElements[0];
            representation.setFile(rootStackTraceElement.getFileName());
            representation.setLine(rootStackTraceElement.getLineNumber());
        }

        representation.setContext(getContext(errorTrigger));

        MoroiEntry moroiEntry = new MoroiEntry();
        MoroiEntryData moroiEntryData = moroiEntry.getData();
        moroiEntryData.setType(MOROI_MESSAGE_TYPE_EXCEPTION);
        moroiEntryData.setAttributes(representation);

        return moroiEntry;
    }

    private Context getContext(final ErrorTrigger errorTrigger) {
        Context context = new Context();
        List<ContextData> contextDataList = context.getData();

        Iterator<TrackedScope> trackedScopeIterator = errorTrigger.getTrackedScopeIterator();

        while (trackedScopeIterator.hasNext()) {
            TrackedScope currScope = trackedScopeIterator.next();

            ContextData contextData = new ContextData();
            Object source = currScope.getSource();
            //Class<?> sourceClass = source instanceof Class<?> ? (Class<?>) source : source.getClass();

            contextData.setClazz(getSourceName(source));
            contextData.setMethod(currScope.getMethodName());
            contextData.setVariables(getVariables(currScope));

            contextDataList.add(contextData);
        }

        return context;
    }

    private List<Variable> getVariables(final TrackedScope currScope) {
        List<Variable> variables = new LinkedList<>();

        Map<String, TrackedValue> references = currScope.getReferences();
        for (TrackedValue trackedValue : references.values()) {
            Variable var = new Variable();
            Object trackedValueVal = trackedValue.getValue();

            var.setName(trackedValue.getName());
            var.setType(trackedValueVal == null ? "null" : getClassName(trackedValueVal.getClass()));
            var.setValue(varValueToString(trackedValueVal));

            variables.add(var);
        }

        return variables;
    }

    private String varValueToString(final Object trackedValueVal) {
        // FIXME (pal) : I think we should have a strategy pattern for the tostring
        // method here
        try {
            // valueOf is used to avoid NPE
            return String.valueOf(trackedValueVal);
        } catch (Exception e) {
            return "ERROR calling toString()";
        }
    }

    private long getMoroiTimestamp() {
        return Math.round(System.currentTimeMillis() / 1000);
    }

    private String getSourceName(final Object source) {
        // in case of static methods, GhostWriter passes the Class<?> reference
        if (source instanceof Class<?>) {
            Class<?> klazz = (Class<?>) source;
            return klazz.getName();
        } else {
            return source.getClass().getName();
        }
    }

    private StackTraceElement[] limitedStackTrace(final Throwable throwable, final int stackTraceLimit) {
        boolean stackTraceLimitDisabled = stackTraceLimit < 1;
        final StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTraceLimitDisabled) {
            return stackTrace;
        }

        return Arrays.copyOfRange(stackTrace, 0, stackTraceLimit);
    }

    private String getExceptionName(final Throwable throwable) {
        return getClassName(throwable.getClass());
    }

    private String getClassName(final Class<?> class1) {
        // FIXME (pal): Handle anonymous inner class naming
        return String.valueOf(class1.getCanonicalName() == null ? "anonymous inner class" : class1.getCanonicalName());
    }

    private String stackTraceToString(final Throwable throwable) {
        StringWriter stackTraceWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTraceWriter));
        return stackTraceWriter.toString();
    }

}
