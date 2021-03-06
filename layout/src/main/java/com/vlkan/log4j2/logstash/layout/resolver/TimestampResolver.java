package com.vlkan.log4j2.logstash.layout.resolver;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;

import java.io.IOException;

class TimestampResolver implements EventResolver {

    private static final EventResolver MILLIS_RESOLVER = (logEvent, jsonGenerator) -> {
        long timeMillis = logEvent.getTimeMillis();
        jsonGenerator.writeNumber(timeMillis);
    };

    private static final EventResolver NANOS_RESOLVER = (logEvent, jsonGenerator) -> {
        long nanoTime = logEvent.getNanoTime();
        jsonGenerator.writeNumber(nanoTime);
    };

    private final EventResolver internalResolver;

    TimestampResolver(EventResolverContext context, String key) {
        this.internalResolver = createInternalResolver(context, key);
    }

    private static EventResolver createInternalResolver(EventResolverContext context, String key) {
        if (key == null) {
            return createFormatResolver(context);
        }
        switch (key) {
            case "millis": return MILLIS_RESOLVER;
            case "nanos": return NANOS_RESOLVER;
        }
        throw new IllegalArgumentException("unknown key: " + key);
    }

    private static EventResolver createFormatResolver(EventResolverContext context) {
        return (logEvent, jsonGenerator) -> {
            long timestampMillis = logEvent.getTimeMillis();
            FastDateFormat timestampFormat = context.getTimestampFormat();
            String timestamp = timestampFormat.format(timestampMillis);
            jsonGenerator.writeString(timestamp);
        };
    }

    static String getName() {
        return "timestamp";
    }

    @Override
    public void resolve(LogEvent logEvent, JsonGenerator jsonGenerator) throws IOException {
        internalResolver.resolve(logEvent, jsonGenerator);
    }

}
