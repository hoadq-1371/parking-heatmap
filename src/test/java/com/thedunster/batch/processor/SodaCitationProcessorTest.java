package com.thedunster.batch.processor;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GeocodingApi.class, GeocodingApiRequest.class})
public class SodaCitationProcessorTest {
    @InjectMocks
    SodaCitationProcessor processor = new SodaCitationProcessor();
    @Mock
    LocationRepository locationRepositoryMock;

    @Mock
    CarRepository carRepositoryMock;

    @Mock
    ViolationRepository violationRepositoryMock;

    @Mock
    CitationRepository citationRepositoryMock;

    SodaCitation sodaCitation;

    @Mock
    private GeocodingApiRequest geocodingApiRequestMock;

    @Before
    public void setup() throws Exception {
        mockStatic(GeocodingApi.class);
        LatLng latLng = new LatLng(1, 5);
        Geometry geometry = new Geometry();
        geometry.location = latLng;
        GeocodingResult geocodingResult = new GeocodingResult();
        geocodingResult.types = new AddressType[]{AddressType.LOCALITY};
        geocodingResult.formattedAddress = "101 Hollins St Baltimore, MD";
        geocodingResult.geometry = geometry;

        GeocodingResult[] geocodingResults = new GeocodingResult[]{geocodingResult};
        when(geocodingApiRequestMock.await()).thenReturn(geocodingResults);
        when(GeocodingApi.geocode(any(GeoApiContext.class), anyString()))
                .thenReturn(geocodingApiRequestMock);

        sodaCitation = new SodaCitation("Citation", "Tag", "Make", "location", "state", "description", BigDecimal.TEN, "2011-12-09T11:55:00" , "Violation Code");
    }

    @Test
    public void previousCitationEntityExists() throws Exception {
        List<CitationEntity> citationEntities = new ArrayList<>();
        CitationEntity citationEntity = new CitationEntity.Builder().build();
        citationEntities.add(citationEntity);
        when(citationRepositoryMock.findByCitation(anyString())).thenReturn(citationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        verify(citationRepositoryMock).findByCitation(anyString());
        assertNull(citationEntityProcessed);
    }


    @Test
    public void couldNotFindPreviousCitationEntity() throws Exception {
        List<CitationEntity> citationEntities = new ArrayList<>();
        when(citationRepositoryMock.findByCitation(anyString())).thenReturn(citationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        verify(citationRepositoryMock).findByCitation(anyString());
        verify(carRepositoryMock).findByTagStateAndMake(anyString(), anyString(), anyString());
        assertNotNull(citationEntityProcessed);
    }

    @Test
    public void previousCarEntityExists() throws Exception {
        final String tag = "TEST_TAG";
        final String make = "MAKE";
        final String state = "STATE";
        List<CarEntity> carEntities = new ArrayList<>();
        CarEntity carEntity = new CarEntity.Builder()
                .tag(tag)
                .make(make)
                .state(state)
                .build();
        carEntities.add(carEntity);

        when(carRepositoryMock.findByTagStateAndMake(anyString(), anyString(), anyString())).thenReturn(carEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        assertEquals(tag, citationEntityProcessed.getCar().getTag());
        assertEquals(state, citationEntityProcessed.getCar().getState());
        assertEquals(make, citationEntityProcessed.getCar().getMake());
    }

    @Test
    public void couldNotFindPreviousCarEntity() throws Exception {
        List<CarEntity> carEntities = new ArrayList<>();

        when(carRepositoryMock.findByTagStateAndMake(anyString(), anyString(), anyString())).thenReturn(carEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);

        assertEquals(sodaCitation.getTag(), citationEntityProcessed.getCar().getTag());
        assertEquals(sodaCitation.getState(), citationEntityProcessed.getCar().getState());
        assertEquals(sodaCitation.getMake(), citationEntityProcessed.getCar().getMake());
    }

    @Test
    public void previousViolationEntityExists() {
        final String description = "description";
        final BigDecimal fine = BigDecimal.valueOf(102);
        List<ViolationEntity> violationEntities = new ArrayList<>();
        ViolationEntity violationEntity = new ViolationEntity.Builder()
                .description(description)
                .fine(fine)
                .build();
        violationEntities.add(violationEntity);

        when(violationRepositoryMock.findByDescription(anyString())).thenReturn(violationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        assertEquals(description, citationEntityProcessed.getViolation().getDescription());
        assertEquals(fine, citationEntityProcessed.getViolation().getFine());
        assertFalse(citationEntityProcessed.getViolation().getSimulatedFine());

    }

    /**
     * If the api blows up again with no fine
     */
    @Test
    public void simulateViolationFine() {
        final String description = "description";
        List<ViolationEntity> violationEntities = new ArrayList<>();
        ViolationEntity violationEntity = new ViolationEntity.Builder()
                .description(description)
                .fine(null)
                .build();
        violationEntities.add(violationEntity);

        when(violationRepositoryMock.findByDescription(anyString())).thenReturn(violationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        assertEquals(description, citationEntityProcessed.getViolation().getDescription());
        assertNotNull(citationEntityProcessed.getViolation().getFine());
        assertTrue(citationEntityProcessed.getViolation().getSimulatedFine());

    }


    @Test
    public void couldNotFindPreviousViolationEntity(){
        List<ViolationEntity> violationEntities = new ArrayList<>();

        when(violationRepositoryMock.findByDescription(anyString())).thenReturn(violationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);

        assertEquals(sodaCitation.getDescription(), citationEntityProcessed.getViolation().getDescription());
        assertEquals(sodaCitation.getViolFine(), citationEntityProcessed.getViolation().getFine());
        assertEquals(false, citationEntityProcessed.getViolation().getSimulatedFine());

    }

    @Test
    public void previousLocationEntityExists() {
        final String longitude = "12";
        final String latitude = "13";
        final String street = "123 Sesame Street";
        List<LocationEntity> locationEntities = new ArrayList<>();
        LocationEntity locationEntity = new LocationEntity.Builder()
                .longitude(longitude)
                .latitude(latitude)
                .streetAddress(street)
                .build();
        locationEntities.add(locationEntity);

        when(locationRepositoryMock.findByStreetAddress(anyString())).thenReturn(locationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);
        assertEquals(latitude, citationEntityProcessed.getLocation().getLatitude());
        assertEquals(longitude, citationEntityProcessed.getLocation().getLongitude());
        assertEquals(street, citationEntityProcessed.getLocation().getStreetAddress());
    }

    @Test
    public void couldNotFindPreviousLocationEntity() {
        List<LocationEntity> locationEntities = new ArrayList<>();

        when(locationRepositoryMock.findByStreetAddress(anyString())).thenReturn(locationEntities);
        CitationEntity citationEntityProcessed = processor.process(sodaCitation);

        assertEquals(sodaCitation.getLocation(), citationEntityProcessed.getLocation().getStreetAddress());
        assertNotNull(citationEntityProcessed.getLocation().getLatitude());
        assertNotNull(citationEntityProcessed.getLocation().getLongitude());

    }
}

