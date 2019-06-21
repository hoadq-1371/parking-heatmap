package com.thedunster.jpa.entities;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "location")
@ToString
public class LocationEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Getter
    private String streetAddress;

    @Getter
    private String latitude;

    @Getter
    private String longitude;

    private LocationEntity(Builder builder) {
        this.streetAddress = builder.streetAddress;
        this.longitude = builder.longitude;
        this.latitude = builder.latitude;
    }

    public LocationEntity() {
    }

    public static class Builder {
        private String streetAddress;
        private String latitude;
        private String longitude;

        public Builder() {
        }

        public Builder streetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        public Builder latitude(String latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public LocationEntity build() {
            return new LocationEntity(this);
        }
    }
}
