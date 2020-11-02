package priv.cyx.java.elasticsearch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import priv.cyx.java.elasticsearch.service.JdSearchService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class JdSearchController {

    @Autowired
    private JdSearchService jdSearchService;

    @GetMapping("/jd-goods/add/{keyword}")
    public String addJdGoodsByKeyword(@PathVariable("keyword") String keyword) throws IOException {
        return Boolean.toString(jdSearchService.addJdGoods(keyword));
    }

    @GetMapping("/jd-goods/search/{keyword}/{pageNo}/{pageSize}")
    public String searchGoodsByKeyword(
            @PathVariable("keyword") String keyword,
            @PathVariable("pageNo") int pageNo,
            @PathVariable("pageSize") int pageSize) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> jdGoods = jdSearchService.searchJdGoods(keyword, pageNo, pageSize);
        return mapper.writeValueAsString(jdGoods);
    }

    @GetMapping("/jd-goods/create/index")
    public String createJdSearchDemoIndex() throws IOException {
        return Boolean.toString(jdSearchService.createJdSearchDemoIndex());
    }
}
