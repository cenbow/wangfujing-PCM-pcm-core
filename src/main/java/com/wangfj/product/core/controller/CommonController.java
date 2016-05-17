package com.wangfj.product.core.controller;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wangfj.core.framework.base.controller.BaseController;
import com.wangfj.core.utils.JsonUtil;
import com.wangfj.core.utils.ResultUtil;
import com.wangfj.product.stocks.service.intf.IPcmLockAttributeService;

@Controller
@RequestMapping("/common")
public class CommonController extends BaseController {
	@Autowired
	private IPcmLockAttributeService pcmLockAttributeService;

	@RequestMapping("/reset")
	@ResponseBody
	public Map<String, Object> reset() throws Exception {
		pcmLockAttributeService.resetAttribute();
		return ResultUtil.creComSucResult("");
	}

	@RequestMapping("/test")
	@ResponseBody
	public Map<String, Object> test() throws Exception {
		return ResultUtil.creComSucResult("");
	}

	@RequestMapping("/getIP")
	@ResponseBody
	public String getIP() throws Exception {
		List<String> ipList = new ArrayList<String>();
		try {
			InetAddress ip = null;
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces(); interfaces.hasMoreElements();) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback() || networkInterface.isVirtual()
						|| !networkInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = addresses.nextElement();
					ipList.add(ip.isSiteLocalAddress() + "," + ip.isLoopbackAddress() + ","
							+ ip.getHostAddress());
					if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
							&& ip.getHostAddress().indexOf(":") == -1) {
						ipList.add(ip.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
		}
		return JsonUtil.getJSONString(ipList);
	}

}
