package com.thedunster.batch.processor;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.thedunster.common.SodaCitation;
import com.thedunster.jpa.entities.CarEntity;
import com.thedunster.jpa.entities.CitationEntity;
import com.thedunster.jpa.entities.LocationEntity;
import com.thedunster.jpa.entities.ViolationEntity;
import com.thedunster.jpa.repository.CarRepository;
import com.thedunster.jpa.repository.CitationRepository;
import com.thedunster.jpa.repository.LocationRepository;
import com.thedunster.jpa.repository.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Processes Soda Json Object into Entities.
 */
public class SodaCitationProcessor implements ItemProcessor<SodaCitation, CitationEntity> {
    @Autowired
    LocationRepository locationRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    ViolationRepository violationRepository;

    @Autowired
    CitationRepository citationRepository;

    @Value("${parking.heatmap.key.google.api.key}")
    private String googleApiKey;

    private static final Logger log = LoggerFactory.getLogger(SodaCitationProcessor.class);

    /**
     * Geolocation API has limit of 2.5k calls a day. Thanks Scroogle.
     */
    private GeoApiContext geoApiContext = null;

    @PostConstruct
    public void init() {
        geoApiContext = new GeoApiContext().setApiKey(googleApiKey);
    }

    /**
     * Main Processor - Checks to see if the citation already exists or not.
     *
     * @param sodaCitation Json Object with data
     * @return citation entity to save
     */
    @Override
    public CitationEntity process(SodaCitation sodaCitation) {
        final String method = "process()";
        CitationEntity citationEntity = null;
        List<CitationEntity> results = citationRepository.findByCitation(sodaCitation.getCitation());
        if (results.size() == 0) {
            CarEntity carEntity = getCarEntity(sodaCitation);
            LocationEntity locationEntity = getLocationEntity(sodaCitation);
            ViolationEntity violationEntity = getViolationEntity(sodaCitation);

            citationEntity = new CitationEntity.Builder()
                    .citation(sodaCitation.getCitation())
                    .car(carEntity)
                    .location(locationEntity)
                    .violation(violationEntity)
                    .date(parseTimeStamp(sodaCitation.getViolDate()))
                    .build();
            log.info("{}: Received response object: {} and converted to: {}", method, sodaCitation, citationEntity);
        }

        return citationEntity;
    }

    /**
     * Loads violation entity from soda if not found.
     *
     * @param sodaCitation
     * @return
     */
    private ViolationEntity getViolationEntity(SodaCitation sodaCitation) {
        final String method = "getViolationEntity";
        log.info("Received Response Violation Description: {}", sodaCitation.getDescription());
        List<ViolationEntity> violationEntities = violationRepository.findByDescription(sodaCitation.getDescription());
        ViolationEntity violationEntity;
        if (violationEntities.size() != 1) {
            violationEntity = new ViolationEntity.Builder()
                    .description(sodaCitation.getDescription())
                    .fine(sodaCitation.getViolFine())
                    .build();
        } else {
            violationEntity = violationEntities.get(0);
        }
        return violationEntity;

    }

    /**
     * Load location entity from soda if not found.
     * Queries Google GeoAPI to get lat and long.
     *
     * @param sodaCitation - json data
     * @return location entity
     */
    private LocationEntity getLocationEntity(SodaCitation sodaCitation) {
        final String method = "getLocationEntity";
        LocationEntity locationEntity;
        List<LocationEntity> locationEntities = locationRepository.findByStreetAddress(sodaCitation.getLocation());
        if (locationEntities.size() != 1) {
            log.info("{}: Calling google api", method);
            GeocodingResult[] results = new GeocodingResult[0];
            try {
                results = GeocodingApi.geocode(geoApiContext, sodaCitation.getLocation() + " Baltimore, MD").await();
            } catch (Exception e) {
                log.error("Exception caught while calling google geocoding api: {}", e);
            }
            LatLng latLng = results[0].geometry.location;
            log.info("{}: Google Geocoding results: {}", method, latLng);

            String longitude = String.valueOf(latLng.lng);
            String latitude = String.valueOf(latLng.lat);
            locationEntity = new LocationEntity.Builder()
                    .streetAddress(sodaCitation.getLocation())
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        } else {
            locationEntity = locationEntities.get(0);
        }
        return locationEntity;
    }

    /**
     * Loads car entity from soda if not found
     *
     * @param sodaCitation
     * @return
     */
    private CarEntity getCarEntity(SodaCitation sodaCitation) {
        String make = sodaCitation.getMake();
        if (make == null) {
            make = "OTH";
        }
        List<CarEntity> carEntities = carRepository.findByTagStateAndMake(sodaCitation.getTag(), sodaCitation.getState(), make);
        CarEntity carEntity;
        if (carEntities.size() != 1) {
            carEntity = new CarEntity.Builder()
                    .state(sodaCitation.getState())
                    .make(sodaCitation.getMake())
                    .tag(sodaCitation.getTag())
                    .build();
        } else {
            carEntity = carEntities.get(0);
        }
        return carEntity;
    }

    public Timestamp parseTimeStamp(String date) {
        Timestamp timestamp = null;
        if (date != null) {
            timestamp = Timestamp.valueOf(LocalDateTime.parse(date));
        }
        return timestamp;
    }
}
