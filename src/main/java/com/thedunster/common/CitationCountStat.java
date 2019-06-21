package com.thedunster.common;

import lombok.Getter;
import lombok.ToString;

@ToString
public class CitationCountStat {

    @Getter
    private final String make;

    @Getter
    private final String count;

    public CitationCountStat(String count,
                             String make) {
        this.count = count;
        this.make = make;
    }


}
