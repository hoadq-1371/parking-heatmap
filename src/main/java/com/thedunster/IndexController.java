package com.thedunster;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    String index(){
        return "index";
    }

    @RequestMapping("/heatmap-click")
    String heatmapclick(){
        return "heatmap-click";
    }

    @RequestMapping("/heatmap-mouseover")
    String heatmapmouseover(){
        return "heatmap-mouseover";
    }
}
