package com.thedunster.rest.controller;

import com.thedunster.jpa.entities.CitationEntity;
import com.thedunster.jpa.repository.CitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/citation")
public class CitationController {
    private static final Logger log = LoggerFactory.getLogger(CitationController.class);

    /**
     * Off set a timestamp by 1 HR.
     */
    private final int OFFSET_TO_NEXT_HOUR = 59 * 60 * 1000 + 59 * 1000 + 999;

    @Autowired
    CitationRepository citationRepository;

    /**
     * Gets citations based on parameters.
     *
     * @param timestampParameter - time to search on
     * @param make               - filter by makes
     * @param ticketType         - filter by ticket types.
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<CitationEntity> index(@RequestParam(value = "timestamp", required = false) String timestampParameter,
                                      @RequestParam(value = "make", required = false) List<String> make,
                                      @RequestParam(value = "ticket", required = false) List<String> ticketType) {
        final String method = "index";
        log.info("{}: Get Request with timestamp: {}, make: {}, ticketType: {}", method,
                timestampParameter, make, ticketType);

        Long timestamp = parseTimestamp(timestampParameter);

        List<CitationEntity> citationEntities = getCitationEntities(timestamp);

        filterOnMake(make, citationEntities);
        filterOnTicketType(ticketType, citationEntities);

        return citationEntities;
    }

    /**
     * Gets the citation based on time stamp
     *
     * @param timestamp
     * @return
     */
    private List<CitationEntity> getCitationEntities(Long timestamp) {
        final String method = "getCitationEntities";
        List<CitationEntity> citationEntities;
        if (timestamp == null) {
            citationEntities = (List<CitationEntity>) citationRepository.findAll();
        } else {
            Timestamp startTime = new Timestamp(timestamp);
            Timestamp endTime = new Timestamp(timestamp + OFFSET_TO_NEXT_HOUR);
            log.info("{}: Time range search starting from {} until {}", method, startTime, endTime);
            citationEntities = citationRepository.findByRange(startTime, endTime);
        }
        return citationEntities;
    }

    /**
     * Converts the string timestamp into a Long. Needs to account for null time stamp.
     *
     * @param timestampParameter
     * @return
     */
    private Long parseTimestamp(@RequestParam(value = "timestamp", required = false) String timestampParameter) {
        final String method = "parseTimestamp";
        Long timestamp = null;
        try {
            timestamp = Long.valueOf(timestampParameter);
        } catch (NumberFormatException e) {
            log.info("{} invalid number passed: {}. Most likely null.", method, timestampParameter);
        }
        return timestamp;
    }

    /**
     * Filter results on ticket type.
     *
     * @param ticketTypes
     * @param citationEntities
     */
    private void filterOnTicketType(@RequestParam(value = "ticket", required = false) List<String> ticketTypes, List<CitationEntity> citationEntities) {
        if (ticketTypes != null) {
            citationEntities.removeIf(p ->
                    !ticketTypes.contains(p.getViolation().getId().toString()));
        }
    }

    /**
     * Filter results on make.
     *
     * @param makes
     * @param citationEntities
     */
    private void filterOnMake(@RequestParam(value = "make", required = false) List<String> makes, List<CitationEntity> citationEntities) {
        if (makes != null) {
            citationEntities.removeIf(p ->
                    !makes.contains(p.getCar().getMake())
            );
        }
    }

}
