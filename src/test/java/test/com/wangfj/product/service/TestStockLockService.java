package test.com.wangfj.product.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wangfj.product.stocks.domain.vo.StockProCountDto;
import com.wangfj.product.stocks.service.intf.IPcmStockLockRecordService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class TestStockLockService {
	@Autowired
	IPcmStockLockRecordService pcmStockLockRecordService;

	/* 查询锁库数量 */
	@Test
	public void selectLockCountByShoppePro() {
		StockProCountDto record = new StockProCountDto();
		record.setSalesItemNo("10012556");
		record.setSupplyProductNo("100123");
		Integer lockCount = pcmStockLockRecordService.selectLockCountByShoppePro(record);
		System.out.println(lockCount);
	}

	/* 判断是否锁库 */
	@Test
	public void lockStockStatus() {
		StockProCountDto record = new StockProCountDto();
		record.setSalesItemNo("10012556");
		record.setSupplyProductNo("100123");
		Integer lockCount = pcmStockLockRecordService.lockStockStatus(record);
		System.out.println(lockCount);
	}
	
	/*添加锁库记录*/
	@Test
	public void lockStock(){
		StockProCountDto record = new StockProCountDto();
		record.setSalesItemNo("10012556");
		record.setSupplyProductNo("100123");
		record.setStockType(1004);
		record.setSaleSum(200);
//		boolean lockStock = pcmStockLockRecordService.lockStock(record);
//		System.out.println(lockStock);
	}
	
	/*添加解锁记录*/
	@Test
	public void unlockStock(){
		StockProCountDto record = new StockProCountDto();
		record.setSalesItemNo("10012556");
		record.setSupplyProductNo("100123");
		record.setStockType(1001);
		record.setSaleSum(200);
		boolean lockStock = pcmStockLockRecordService.unlockStock(record);
		System.out.println(lockStock);
	}
}
