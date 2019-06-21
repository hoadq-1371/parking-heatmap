package com.thedunster.rest.controller;

import com.thedunster.jpa.entities.ViolationEntity;
import com.thedunster.jpa.repository.ViolationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/citation/violation")
public class ViolationController {
    private static final Logger log = LoggerFactory.getLogger(ViolationController.class);

    @Autowired
    public ViolationRepository violationRepository;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ViolationEntity> index() {
        List<ViolationEntity> violationEntities = (List<ViolationEntity>) violationRepository.findAll();
        return violationEntities;
    }

}
