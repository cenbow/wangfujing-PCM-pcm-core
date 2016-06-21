package com.wangfj.product.core.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wangfj.core.constants.ComErrorCodeConstants.ErrorCode;
import com.wangfj.core.framework.base.controller.BaseController;
import com.wangfj.core.framework.exception.BleException;
import com.wangfj.core.utils.HttpUtil;
import com.wangfj.core.utils.JsonUtil;
import com.wangfj.core.utils.PropertyUtil;
import com.wangfj.core.utils.ResultUtil;
import com.wangfj.core.utils.ThrowExcetpionUtil;
import com.wangfj.product.common.domain.vo.PcmExceptionLogDto;
import com.wangfj.product.common.service.intf.IPcmExceptionLogService;
import com.wangfj.product.constants.StatusCodeConstants.StatusCode;
import com.wangfj.product.core.controller.support.PcmEdiProductStockPara;
import com.wangfj.product.core.controller.support.PcmStockWCSPara;
import com.wangfj.product.core.controller.support.StockProCountListPara;
import com.wangfj.product.core.controller.support.StockProCountPara;
import com.wangfj.product.core.controller.support.base.constants.CommonParamValidate;
import com.wangfj.product.stocks.domain.vo.PcmProductStockInfoDto;
import com.wangfj.product.stocks.domain.vo.StockProCountDto;
import com.wangfj.product.stocks.domain.vo.StockProCountListDto;
import com.wangfj.product.stocks.domain.vo.StockResultDto;
import com.wangfj.product.stocks.service.intf.IPcmStockService;
import com.wangfj.util.Constants;

/**
 * 库存管理
 * 
 * @Class Name PcmStockController
 * @Author yedong
 * @Create In 2015年7月20日
 */
@Controller
@RequestMapping("/stock")
public class PcmStockController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(PcmStockController.class);

	@Autowired
	private IPcmStockService pcmStockService;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private IPcmExceptionLogService pcmExceptionLogService;

	/**
	 * 加减库
	 * 
	 * @Methods Name findInsertAndReduceFromPcm
	 * @Create In 2015年8月12日 By yedong
	 * @param stockProCountListPara
	 * @return String
	 */
	@ResponseBody
	@RequestMapping(value = "/findInsertAndReduceFromPcm", produces = "application/json; charset=utf-8")
	public Map<String, Object> findInsertAndReduceFromPcm(
			@RequestBody @Valid StockProCountListPara stockProCountListPara) {
		final StockProCountListDto stockProCountListDto = new StockProCountListDto();
		List<StockProCountPara> listProCountPara = stockProCountListPara.getProducts();
		List<StockProCountDto> listProCountDto = new ArrayList<StockProCountDto>();
		List<PcmProductStockInfoDto> listSkuStockDto = new ArrayList<PcmProductStockInfoDto>();
		PcmProductStockInfoDto skuStockDto = null;
		final List<String> proList = new ArrayList<String>();
		final PcmEdiProductStockPara pushList = new PcmEdiProductStockPara();
		try {
			BeanUtils.copyProperties(stockProCountListDto, stockProCountListPara);
			for (StockProCountPara para : listProCountPara) {
				StockProCountDto dto = new StockProCountDto();
				BeanUtils.copyProperties(dto, para);
				if (StringUtils.isBlank(dto.getIsPayReduce())) {
					dto.setIsPayReduce(Constants.PCMSTOCK_ISPAY_REDUCESTOCK0);
				}
				if (StringUtils.isBlank(dto.getChannelSid())) {
					dto.setChannelSid(Constants.DEFAULT_CHANNEL_SID);
				}
				listProCountDto.add(dto);
				skuStockDto = new PcmProductStockInfoDto();
				skuStockDto.setShoppeProSid(dto.getSupplyProductNo());
				pushList.setChannelCode(dto.getChannelSid());
				proList.add(dto.getSupplyProductNo());
				listSkuStockDto.add(skuStockDto);
			}
			stockProCountListDto.setProducts(listProCountDto);
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage());
		}
		/* 数量满足200条以内 向下执行 */
		StockResultDto resultDto = new StockResultDto();
		if (listProCountDto.size() < Constants.STOCK_LINE_COUNT) {
			try {
				if (Constants.PCM_ISOFFERLINE0
						.equals(stockProCountListDto.getIsOfferLine().toString())) {
					resultDto = pcmStockService.findInsertAndReduceFromPcmV2(stockProCountListDto);
					pcmStockService.updateSKUStatus(listSkuStockDto);
					pushOrderStockToWCS(stockProCountListDto);
				} else if (Constants.PCM_ISOFFERLINE1
						.equals(stockProCountListDto.getIsOfferLine().toString())) {
					StockResultDto resultDtoOffLine = new StockResultDto();
					resultDto.setResultFlag(Constants.PCM_OPERATION_SUCCEED);
					try {
						resultDtoOffLine = pcmStockService
								.findInsertAndReduceFromPcmByOffLine(stockProCountListDto);
						pcmStockService.updateSKUStatus(listSkuStockDto);
						pushOrderStockToWCS(stockProCountListDto);
					} catch (BleException e) {
						resultDtoOffLine.setResultFlag(Constants.PCM_OPERATION_FAILED);
						resultDtoOffLine.setResultMsg(e.getMessage());
						logger.error("API,findInsertAndReduceFromPcm.htm,Error:" + e.getMessage());
					}
					try {
						if (resultDtoOffLine.getResultFlag()
								.equals(Constants.PCM_OPERATION_FAILED)) {
							SavaErrorMessage(
									JsonUtil.getJSONString(resultDtoOffLine.getResultMsg()),
									JsonUtil.getJSONString(stockProCountListDto));
						}
					} catch (Exception e) {
						logger.error("API,findInsertAndReduceFromPcm.htm,Add Exception Error:"
								+ e.getMessage());
					}
					if (stockProCountListDto.getCzType() == Constants.PCMSTOCK_OPERATION_TYPE0) {// 锁库
						return ResultUtil.creComSucResult(ErrorCode.STOCK_LOCK_SUCCEED.getMemo());
					} else if (stockProCountListDto
							.getCzType() == Constants.PCMSTOCK_OPERATION_TYPE1) {// 解锁
						return ResultUtil.creComSucResult(ErrorCode.STOCK_REDUCE_SUCCEED.getMemo());
					} else {
						return ResultUtil.creComSucResult(resultDtoOffLine.getResultMsg());
					}
				}
			} catch (BleException e) {
				resultDto.setResultFlag(Constants.PCM_OPERATION_FAILED);
				resultDto.setResultMsg(e.getMessage());
				if (e.getCode().equals(ErrorCode.STOCK_LOCK_FAILED_ERROR.getMemo())
						|| e.getCode().equals(ErrorCode.STOCK_REDUCE_FAILED_ERROR.getMemo())
						|| e.getCode().equals(ErrorCode.STOCK_UNLOCK_FAILED_ERROR.getMemo())
						|| e.getCode().equals(ErrorCode.STOCK_REFUND_FAILED_ERROR.getMemo())
						|| e.getCode().equals(ErrorCode.STOCK_OPERATION_FAILED_ERROR.getMemo())) {
					ThrowExcetpionUtil
							.splitExcetpion(new BleException(e.getCode(), e.getMessage()));
				}
			}
		} else {
			resultDto.setResultFlag(Constants.PCM_OPERATION_FAILED);
			resultDto.setResultMsg(ErrorCode.STOCK_IMPORT_ERROR.getMemo());
		}
		if (resultDto.getResultFlag().equals(Constants.PCM_OPERATION_SUCCEED)) {
			// 库存下发
			if (proList != null && proList.size() > 0) {
				taskExecutor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							pushList.setShoppeProSids(proList);
							String ediStockUrl = PropertyUtil.getSystemUrl("edi.stock");
							logger.info("API,synPushStockToEDI,request:" + pushList.toString());
							String response = HttpUtil.doPost(ediStockUrl,
									JsonUtil.getJSONString(pushList));
							logger.info("API,synPushStockToEDI,response:" + response);
						} catch (Exception e) {
							logger.error("API,synPushStockToEDI,Error:" + e.getMessage());
							ThrowExcetpionUtil.splitExcetpion(new BleException(
									ErrorCode.STOCK_IMPORT_PUSH_ERROR.getErrorCode(),
									ErrorCode.STOCK_IMPORT_PUSH_ERROR.getMemo() + e.getMessage()));
							SavaErrorMessage(e.getMessage(), JsonUtil.getJSONString(pushList));
						}
					}
				});
			}
			return ResultUtil.creComSucResult(resultDto.getResultMsg());
		} else {
			return ResultUtil.creComErrorResult(resultDto.getResultFlag(),
					resultDto.getResultMsg());
		}
	}

	/**
	 * 订单变更库存数推送给WCS
	 * 
	 * @Methods Name pushOrderStockToWCS
	 * @Create In 2016年6月20日 By kongqf
	 * @param dto
	 *            void
	 */
	public void pushOrderStockToWCS(final StockProCountListDto dto) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (!CommonParamValidate.WCS.equals(dto.getFromSystem())) {
					List<PcmStockWCSPara> paraList = new ArrayList<PcmStockWCSPara>();
					PcmStockWCSPara para = null;
					for (StockProCountDto stockProCountDto : dto.getProducts()) {
						para = new PcmStockWCSPara();
						para.setType(getWCSOrderType(stockProCountDto.getStockType(),
								stockProCountDto.getIsPayReduce()));
						para.setMatnr(stockProCountDto.getSupplyProductNo());
						para.setNum(stockProCountDto.getSaleSum());
						if (Constants.PCMSTOCK_ISPAY_REDUCESTOCK1
								.equals(stockProCountDto.getIsPayReduce())) {
							if (Constants.PCMSTOCK_YES_UNLOCK == stockProCountDto.getStockType()) {
								paraList.add(para);
							}
						} else {
							paraList.add(para);
						}
					}
					if (paraList != null && paraList.size() > 0) {
						String wcsStockUrl = PropertyUtil.getSystemUrl("wcs.order.stock");
						logger.info("API,synPushStockToWCS,request:" + paraList.toString());
						String response;
						try {
							response = HttpUtil.doPost(wcsStockUrl,
									JsonUtil.getJSONString(paraList));
							logger.info("API,synPushStockToWCS,response:" + response);
						} catch (Exception e) {
							ThrowExcetpionUtil.splitExcetpion(new BleException(
									ErrorCode.STOCK_IMPORT_PUSH_ERROR.getErrorCode(),
									ErrorCode.STOCK_IMPORT_PUSH_ERROR.getMemo() + wcsStockUrl
											+ e.getMessage()));
							SavaErrorMessage(e.getMessage(), JsonUtil.getJSONString(paraList));
						}
					}
				}
			}
		});
	}

	/**
	 * 获取wcs操作类型
	 * 
	 * @Methods Name getWCSOrderType
	 * @Create In 2016年6月20日 By kongqf
	 * @param stockType
	 * @return String
	 */
	private String getWCSOrderType(Integer stockType, String isPayReduce) {
		String wcsType = "0";
		switch (stockType) {
		case 1023:// 锁库
			wcsType = "1";
			break;
		case 1021:// 减库
			if (Constants.PCMSTOCK_ISPAY_REDUCESTOCK1.equals(isPayReduce)) {
				wcsType = "4";
			} else {
				wcsType = "3";
			}
			break;
		case 1022:// 解锁
			wcsType = "2";
			break;
		}

		return wcsType;
	}

	/**
	 * 保存错误信息
	 * 
	 * @Methods Name SavaErrorMessage
	 * @Create In 2015年12月21日 By kongqf
	 * @param errorMessage
	 * @param dataContent
	 *            void
	 */
	private void SavaErrorMessage(String errorMessage, String dataContent) {
		try {
			PcmExceptionLogDto dto = new PcmExceptionLogDto();
			dto.setInterfaceName("pcm-core/stock/findInsertAndReduceFromPcm");
			dto.setExceptionType(StatusCode.EXCEPTION_STOCK.getStatus());
			dto.setErrorMessage(errorMessage);
			dto.setDataContent(dataContent);
			dto.setUuid(UUID.randomUUID().toString());
			pcmExceptionLogService.saveExceptionLogInfo(dto);
		} catch (Exception e) {
			logger.info("API,Save PcmExceptionLogDto failed:" + e.getMessage());
		}
	}
}
