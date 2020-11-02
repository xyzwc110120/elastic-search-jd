package priv.cyx.java.elasticsearch.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface JdSearchService {

    /**
     * 添加商品信息文档至 ES
     *
     * @param keyword 搜索关键字
     * @return true 添加成功，false 添加失败
     */
    boolean addJdGoods(String keyword) throws IOException;

    /**
     * 从 ES 获取商品信息
     *
     * @param keyword 搜索关键字
     * @param pageNo 起始页面
     * @param pageSize 每页显示信息数量
     * @return 搜索信息
     */
    List<Map<String, Object>> searchJdGoods(String keyword, int pageNo, int pageSize) throws IOException;

    /**
     * 创建索引
     *
     * @return ture 创建成功；false 创建失败
     */
    boolean createJdSearchDemoIndex() throws IOException;
}
