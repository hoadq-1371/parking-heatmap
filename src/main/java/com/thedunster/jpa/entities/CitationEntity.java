package com.thedunster.jpa.entities;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "citation")
@ToString
public class CitationEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Getter
    private String citation;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "car_id")
    @Getter
    private CarEntity car;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "location_id")
    @Getter
    private LocationEntity location;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "violation_id")
    @Getter
    private ViolationEntity violation;

    @Getter
    private Timestamp date;

    @Getter
    private Boolean simulatedDate;

    public CitationEntity() {
    }

    private CitationEntity(Builder builder) {
        this.citation = builder.citation;
        this.car = builder.car;
        this.location = builder.location;
        this.violation = builder.violation;
        this.date = builder.date;
        this.simulatedDate = builder.simulatedDate;
    }

    public static class Builder {
        private String citation;
        private CarEntity car;
        private LocationEntity location;
        private ViolationEntity violation;
        private Timestamp date;

        private Boolean simulatedDate = false;

        public Builder() {
        }

        public Builder citation(String citation) {
            this.citation = citation;
            return this;
        }

        public Builder car(CarEntity car) {
            this.car = car;
            return this;
        }

        public Builder location(LocationEntity location) {
            this.location = location;
            return this;
        }

        public Builder violation(ViolationEntity violation) {
            this.violation = violation;
            return this;
        }

        public Builder date(Timestamp date) {
            Timestamp timestamp = date;
            if (timestamp == null) {
                long offset = Timestamp.valueOf("2016-03-01 00:00:00").getTime();
                long end = Timestamp.valueOf("2016-04-01 00:00:00").getTime();
                long diff = end - offset + 1;
                timestamp = new Timestamp(offset + (long) (Math.random() * diff));
                simulatedDate = true;
            }
            this.date = timestamp;
            return this;
        }

        public CitationEntity build() {
            return new CitationEntity(this);
        }
    }
}
