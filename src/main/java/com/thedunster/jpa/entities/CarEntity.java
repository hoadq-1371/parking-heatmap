package com.thedunster.jpa.entities;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "car")
@ToString
public class CarEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Getter
    private String tag;

    @Getter
    private String state;

    @Getter
    private String make;

    public CarEntity() {}

    private CarEntity(Builder builder) {
        this.tag = builder.tag;
        this.state = builder.state;
        this.make = builder.make;
    }

    public static class Builder {
        private String tag;
        private String state;
        private String make;

        public Builder() {
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder make(String make) {
            this.make = make;
            return this;
        }

        public CarEntity build() {
            return new CarEntity(this);
        }
    }
}
