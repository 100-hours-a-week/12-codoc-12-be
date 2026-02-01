package _ganzi.codoc.global.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.util.Map;
import net.logstash.logback.composite.AbstractJsonProvider;

/**
 * 사용자가 제시한 스키마를 "형식 그대로" 강제한다. { "timestamp": "...", "level": "...", "service": "...", "trace_id":
 * "...", "path": "...", "status": 200, "latency": 0.452, "message": "...", "context": { "user_id":
 * "...", "ip": "..." }, "error": { "type": "...", "stacktrace": "..." } }
 *
 * <p>주의: - timestamp/level/message는 logback provider가 이미 쓰지만, 여기서 누락 없이 "추가/보정"만 한다. - 실제 값들은 MDC에서
 * 읽는다.
 */
public class LogstashFixedSchemaProvider extends AbstractJsonProvider<ILoggingEvent> {

    @Override
    public void writeTo(JsonGenerator gen, ILoggingEvent event) throws IOException {
        Map<String, String> mdc = event.getMDCPropertyMap();

        // service, trace_id, path, status, latency는 최상위
        writeString(gen, "service", mdc.get("service"));
        writeString(gen, "trace_id", mdc.get("trace_id"));
        writeString(gen, "path", mdc.get("path"));

        // status는 숫자. 없으면 null로 둔다
        writeNumberOrNull(gen, "status", mdc.get("status"));

        // latency는 사용자가 준 형식대로 "latency" (단위는 일단 신경 안 씀)
        writeDoubleOrNull(gen, "latency", mdc.get("latency"));

        // context object
        gen.writeFieldName("context");
        gen.writeStartObject();
        writeString(gen, "user_id", mdc.get("context.user_id"));
        writeString(gen, "ip", mdc.get("context.ip"));
        gen.writeEndObject();

        // error object (type/stacktrace)
        gen.writeFieldName("error");
        gen.writeStartObject();
        writeString(gen, "type", mdc.get("error.type"));

        // stacktrace는 error 파일에서는 _stacktrace에 들어갈 수 있으니, MDC 우선 + 없으면 null
        String st = mdc.get("error.stacktrace");
        if (st == null || st.isBlank()) {
            st = null;
        }
        if (st == null) gen.writeNullField("stacktrace");
        else gen.writeStringField("stacktrace", st);

        gen.writeEndObject();
    }

    private void writeString(JsonGenerator gen, String field, String v) throws IOException {
        if (v == null || v.isBlank()) {
            gen.writeNullField(field);
        } else {
            gen.writeStringField(field, v);
        }
    }

    private void writeNumberOrNull(JsonGenerator gen, String field, String v) throws IOException {
        if (v == null || v.isBlank()) {
            gen.writeNullField(field);
            return;
        }
        try {
            gen.writeNumberField(field, Integer.parseInt(v));
        } catch (NumberFormatException e) {
            gen.writeNullField(field);
        }
    }

    private void writeDoubleOrNull(JsonGenerator gen, String field, String v) throws IOException {
        if (v == null || v.isBlank()) {
            gen.writeNullField(field);
            return;
        }
        try {
            gen.writeNumberField(field, Double.parseDouble(v));
        } catch (NumberFormatException e) {
            gen.writeNullField(field);
        }
    }
}
