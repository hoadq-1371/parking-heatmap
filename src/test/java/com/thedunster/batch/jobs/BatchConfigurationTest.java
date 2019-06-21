package com.thedunster.batch.jobs;

import com.socrata.api.Soda2Consumer;
import com.socrata.exceptions.SodaError;
import com.socrata.model.soql.SoqlQuery;
import com.thedunster.common.SodaCitation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.item.support.ListItemReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Soda2Consumer.class)
public class BatchConfigurationTest {

    @Mock
    Soda2Consumer soda2ConsumerMock;

    @Mock
    SodaCitation sodaCitationMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.spy(Soda2Consumer.class);
        when(Soda2Consumer.newConsumer(anyString())).thenReturn(soda2ConsumerMock);
    }

    @Test
    public void sodaCallReturnsNull() throws Exception {
        BatchConfiguration batchConfiguration = new BatchConfiguration();
        List parkingViolations = null;

        when(soda2ConsumerMock.query(anyString(), any(SoqlQuery.class), any())).thenReturn(parkingViolations);

        ListItemReader<SodaCitation> parkingViolationListItemReader = batchConfiguration.reader();
        assertNull(parkingViolationListItemReader);
    }

    @Test
    public void sodaCallReturnsResponse() throws Exception {
        BatchConfiguration batchConfiguration = new BatchConfiguration();
        List parkingViolations = new ArrayList<SodaCitation>();
        parkingViolations.add(sodaCitationMock);

        when(soda2ConsumerMock.query(anyString(), any(SoqlQuery.class), any())).thenReturn(parkingViolations);

        ListItemReader<SodaCitation> parkingViolationListItemReader = batchConfiguration.reader();
        assertNotNull(parkingViolationListItemReader);
    }

    @Test
    public void sodaCallThrowsSodaError() throws Exception {
        BatchConfiguration batchConfiguration = new BatchConfiguration();

        when(soda2ConsumerMock.query(anyString(), any(SoqlQuery.class), any())).thenThrow(new SodaError("NO PHONE HOME"));

        ListItemReader<SodaCitation> parkingViolationListItemReader = batchConfiguration.reader();
        assertNull(parkingViolationListItemReader);
    }

    @Test
    public void sodaCallThrowsInterruptedException() throws Exception {
        BatchConfiguration batchConfiguration = new BatchConfiguration();

        when(soda2ConsumerMock.query(anyString(), any(SoqlQuery.class), any())).thenThrow(new InterruptedException("Surprise MF"));

        ListItemReader<SodaCitation> parkingViolationListItemReader = batchConfiguration.reader();
        assertNull(parkingViolationListItemReader);
    }




}
