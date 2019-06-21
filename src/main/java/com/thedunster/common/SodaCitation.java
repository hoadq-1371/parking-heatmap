package com.thedunster.common;

import com.sun.jersey.api.client.GenericType;
import lombok.Getter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class SodaCitation {
    public static final GenericType<List<SodaCitation>> LIST_TYPE = new GenericType<List<SodaCitation>>() {
    };

    @Getter
    private final String citation;

    @Getter
    private final String tag;

    @Getter
    private final String make;

    @Getter
    private final String state;

    @Getter
    private final String location;

    @Getter
    private final String description;

    @Getter
    private final BigDecimal violFine;

    @Getter
    private final String violDate;

    @Getter
    private final String violCode;

    @JsonCreator
    public SodaCitation(@JsonProperty("citation") String citation,
                        @JsonProperty("tag") String tag,
                        @JsonProperty("make") String make,
                        @JsonProperty("location") String location,
                        @JsonProperty("state") String state,
                        @JsonProperty("description") String description,
                        @JsonProperty("violfine") BigDecimal violFine,
                        @JsonProperty("violdate") String violDate,
                        @JsonProperty("violCode") String violCode) {
        this.citation = citation;
        this.tag = tag;
        this.make = make;
        this.location = location;
        this.state = state;
        this.description = description;
        this.violDate = violDate;
        this.violFine = violFine;
        this.violCode = violCode;
    }


}
