package test.com.wangfj.product.service;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wangfj.product.maindata.service.intf.IPcmShoppeProductService;
import com.wangfj.product.stocks.service.impl.PcmStockLockRecordServiceImpl;
import com.wangfj.product.stocks.service.intf.IPcmStockService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class TestStockService {
	@Autowired
	private IPcmStockService pcmStockService;

	@Autowired
	IPcmShoppeProductService pcmShoppeProSid;

	/*
	 * 是否管库存，是否负库存销售
	 */
	@Test
	public void getStockCounts() {
		/*Map<String, Object> selectStockInfo = pcmShoppeProSid.selectStockInfo("10000022");
		System.out.println(selectStockInfo);*/
	}

	@Autowired
	public PcmStockLockRecordServiceImpl pcmStockLockRecordServiceImpl;

	@Test
	public void testdelStockRecordBySalesItemNo() {

		boolean flag = pcmStockLockRecordServiceImpl.delStockRecordBySalesItemNo("10000000002");
	}

}
