package priv.cyx.java.elasticsearch.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 京东商品信息
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class JdGood {

    private String id;
    private String name;
    private String title;
    private String price;
    private String img;
}
