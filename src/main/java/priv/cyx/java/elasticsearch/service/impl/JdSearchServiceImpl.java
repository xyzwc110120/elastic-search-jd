package priv.cyx.java.elasticsearch.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import priv.cyx.java.elasticsearch.pojo.JdGood;
import priv.cyx.java.elasticsearch.service.JdSearchService;
import priv.cyx.java.elasticsearch.utils.HtmlParseUtil;

import javax.sql.rowset.serial.SerialRef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JdSearchServiceImpl implements JdSearchService {

    private static final String JD_SEARCH_DEMO_INDEX = "jd_search_demo_index";

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Override
    public boolean addJdGoods(String keyword) throws IOException {
        // 解析网页，获取对象集合
        List<JdGood> jdGoodList = HtmlParseUtil.parseJdSearchHtml(keyword);

        // 批量处理请求
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueMinutes(1));
        ObjectMapper objectMapper = new ObjectMapper();
        // 添加数据
        for (JdGood jdGood : jdGoodList) {
            bulkRequest.add(
                    new IndexRequest(JD_SEARCH_DEMO_INDEX)
                            .id(jdGood.getId())
                            .source(objectMapper.writeValueAsString(jdGood), XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        // 因为 hasFailures() 方法是判断是否失败，所以取反来判断是否成功
        return !bulkResponse.hasFailures();
    }

    @Override
    public List<Map<String, Object>> searchJdGoods(String keyword, int pageNo, int pageSize) throws IOException {
        // 判断分页
        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 5;
        }

        SearchRequest searchRequest = new SearchRequest(JD_SEARCH_DEMO_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置查询条件
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", keyword);
        // 加入查询条件
        sourceBuilder.query(termQueryBuilder);

        // 分页设置
        sourceBuilder.from((pageNo - 1) * pageSize).size(pageSize);
        // 设置超时
        sourceBuilder.timeout(TimeValue.timeValueMinutes(1));
        // 高亮设置构造器
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置高亮字段
        highlightBuilder.field("name");
        // 设置前缀
        highlightBuilder.preTags("<span style='color:red'>");
        // 设置后缀
        highlightBuilder.postTags("</span>");
        // 绑定高亮设置
        sourceBuilder.highlighter(highlightBuilder);

        // 将搜索条件放入搜索请求中
        searchRequest.source(sourceBuilder);
        // 发送查询请求并获得响应
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 获取查询结果，进行处理
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<Map<String, Object>> jdGoodList = new ArrayList<>();
        for (SearchHit hit: hits) {
            // 获取文档
            Map<String, Object> jdGood = hit.getSourceAsMap();

            // 将高亮字段替换换来字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("name");
            if (highlightField != null) {
                Text[] fragments = highlightField.fragments();
                StringBuilder highlightName = new StringBuilder();
                for (Text fragment : fragments) {
                    highlightName.append(fragment);
                }
                // 将新的高亮的 name 字段替换成原来的
                jdGood.put("name", highlightName);
            }

            jdGoodList.add(jdGood);
        }
        return jdGoodList;
    }

    @Override
    public boolean createJdSearchDemoIndex() throws IOException {
        // 创建索引
        CreateIndexRequest indexRequest = new CreateIndexRequest(JD_SEARCH_DEMO_INDEX);
        CreateIndexResponse indexResponse = client.indices().create(indexRequest, RequestOptions.DEFAULT);

        // 设置字段
        PutMappingRequest mappingRequest = new PutMappingRequest(JD_SEARCH_DEMO_INDEX);
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("id");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("name");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "ik_smart");
                    builder.field("search_analyzer", "ik_smart");
                }
                builder.endObject();
                builder.startObject("title");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "ik_smart");
                    builder.field("search_analyzer", "ik_smart");
                }
                builder.endObject();
                builder.startObject("price");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
                builder.startObject("img");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        mappingRequest.source(builder);
        AcknowledgedResponse acknowledgedResponse = client.indices().putMapping(mappingRequest, RequestOptions.DEFAULT);

        return indexResponse.isAcknowledged() && acknowledgedResponse.isAcknowledged();
    }
}
