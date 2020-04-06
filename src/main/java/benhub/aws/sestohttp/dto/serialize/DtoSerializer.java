package benhub.aws.sestohttp.dto.serialize;

import benhub.aws.sestohttp.dto.EventContent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class DtoSerializer {

    private final ObjectMapper mapper;

    public DtoSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EventContent deserialize(String json) {
        try {
            return this.mapper.readValue(json, EventContent.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse json: " + json, e);
        }
    }

}
