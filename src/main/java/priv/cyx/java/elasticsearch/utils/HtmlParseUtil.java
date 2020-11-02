package priv.cyx.java.elasticsearch.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import priv.cyx.java.elasticsearch.pojo.JdGood;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 网页解析工具
 */
public class HtmlParseUtil {

    // 京东搜索 URL
    private final static String SEARCH_URL = "https://search.jd.com/Search?enc=utf-8&keyword=";
    // 京东搜索列表模块 id
    private final static String JD_SEARCH_DIV_ID = "J_goodsList";

    private HtmlParseUtil() {
    }

    /**
     * 解析京东搜索网页
     *
     * @param keyword 搜索关键字
     */
    public static List<JdGood> parseJdSearchHtml(String keyword) throws IOException {
        // 解析网页
        // Jsoup.parse() 方法返回的 Document 对象就是浏览器的 Document 对象，我们可以根据该 Document 对象操作解析的网页中的数据
        Document document = Jsoup.parse(new URL(SEARCH_URL + keyword), 10000);
        // 获取搜索的商品列表元素对象
        Element element = document.getElementById(JD_SEARCH_DIV_ID);
        // 获取所有的 li 标签元素（因为有多个 li 标签，所以获取唯一 class 属性的 div 标签）
        Elements liList = element.getElementsByClass("gl-i-wrap");

        // 将获取的信息保存至商品对象集合中
        List<JdGood> goodList = new ArrayList<>();
        // 获取 li 标签元素中的内容
        for (Element li : liList) {
            // 获取 id
            String id = li.parent().attr("data-sku");
            Elements nameElement = li.getElementsByClass("p-name");
            // 获取 li 标签中 class 选择器为 p-name 的商品名称
            String pName = nameElement.select("em").text();
            // 获取说明
            String pTitle = nameElement.select("i").text();
            // 获取价格
            String pPrice = li.select(".p-price i").text();
            // 获取图片地址
            // 注意：因为网站图片很多的话，加载速度会很慢，所以一般图片都是异步懒加载，所以直接获取 img 标签中的 src 属性是获取不到的
            // String img = li.getElementsByTag("img").eq(0).attr("src");
            String img = li.selectFirst("img").attr("data-lazy-img");
            goodList.add(new JdGood(id, pName, pTitle, pPrice, img));
        }
        return goodList;
    }
}
