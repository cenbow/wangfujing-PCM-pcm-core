package com.wangfj.product.core.controller.support;

public class PcmStockWCSPara {

	/**
	 * 操作类型，1.锁库，2.解锁库，3.减库 1.全量，2.增量
	 */
	private String type;
	/**
	 * 商品的编码
	 */
	private String matnr;
	/**
	 * 操作库存的数量
	 */
	private int num;
	/**
	 * 自库或者虚库的标识
	 */
	private String flag;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMatnr() {
		return matnr;
	}

	public void setMatnr(String matnr) {
		this.matnr = matnr;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "PcmStockWCSPara [type=" + type + ", matnr=" + matnr + ", num=" + num + ", flag="
				+ flag + "]";
	}

}
