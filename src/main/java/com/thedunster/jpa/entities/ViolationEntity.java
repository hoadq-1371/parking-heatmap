package com.thedunster.jpa.entities;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "violation")
@ToString
public class ViolationEntity {
    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @Getter
    private String description;

    @Getter
    private BigDecimal fine;

    @Getter
    private Boolean simulatedFine;

    public ViolationEntity() {
    }

    private ViolationEntity(Builder builder) {
        this.description = builder.description;
        this.fine = builder.fine;
        this.simulatedFine = builder.simulatedFine;
    }

    public static class Builder {
        private String description;
        private BigDecimal fine;
        private Boolean simulatedFine = false;

        private final int DOLLAR_MIN = 10;

        private final int DOLLAR_MAX = 500;

        public Builder() {
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder fine(BigDecimal fine) {
            BigDecimal violFine = fine;
            if (violFine == null) {
                violFine = randomDouble();
                simulatedFine = true;
            }
            this.fine = violFine;
            return this;
        }

        public ViolationEntity build() {
            return new ViolationEntity(this);
        }

        private BigDecimal randomDouble() {
            Double doubleValue = ThreadLocalRandom.current().nextDouble(DOLLAR_MIN, DOLLAR_MAX);
            BigDecimal bigDecimal = BigDecimal.valueOf(doubleValue);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
            return bigDecimal;

        }
    }

}
