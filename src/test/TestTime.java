import cn.tycoding.entity.Goods;
import cn.tycoding.mapper.GoodsMapper;
import cn.tycoding.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @auther TyCoding
 * @date 2018/9/21
 * <p>
 * 这个测试只能体现服务器上单个用户访问时的运行性能，客户端的性能还需考虑网络带宽、并发等问题。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring*.xml"})
public class TestTime {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private GoodsService goodsService;


    /**
     * 测试查询数据库的耗时
     * <p>
     * 共 934条数据
     * 平均1000ms
     */
    @Test
    public void run1() {
        Long startTime = System.currentTimeMillis(); //开始时间
        goodsMapper.findAll();
        Long endTime = System.currentTimeMillis(); //结束时间
        System.out.println("查询数据库--共耗时：" + (endTime - startTime) + "毫秒");
    }

    /**
     * 从redis中读取所有数据
     * <p>
     * 第一次运行，redis中还没有数据，是先从数据库中读取放入到redis中 //平均1000毫秒
     * 第二次运行，从redis中读取数据 //平均150毫秒
     */
    @Test
    public void run2() {
        Long startTime = System.currentTimeMillis(); //开始时间

        goodsService.findAll();

        Long endTime = System.currentTimeMillis(); //结束时间
        System.out.println("从redis中读取所有数据，共耗时：" + (endTime - startTime) + "毫秒");
    }

    /**
     * 查询Solr索引库所有数据耗时（分页查询）
     */
    @Test
    public void run3() {
        Long startTime = System.currentTimeMillis(); //开始时间
        Query query = new SimpleQuery("*:*");
        query.setOffset(1);
        query.setRows(1000);
        ScoredPage<Goods> page = solrTemplate.queryForPage(query, Goods.class);
        page.getContent();
        System.out.println("共 " + page.getTotalElements() + "条数据");
        Long endTime = System.currentTimeMillis(); //结束时间
        System.out.println("查询Solr索引库--共耗时：" + (endTime - startTime) + "毫秒"); //804毫秒
    }

}
