package com.saituo.order.web.user;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;
import com.saituo.order.commons.SessionVariable;
import com.saituo.order.commons.VariableUtils;
import com.saituo.order.service.order.BuyCardService;
import com.saituo.order.service.user.AddressService;

@Controller
@RequiresAuthentication
@RequestMapping("order/buycard")
public class BuyCardController {

	@Autowired
	private BuyCardService buyCardService;

	@Autowired
	private AddressService addressService;

	@RequestMapping(value = "addProductToBag/{productId}", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> addProductToBag(@PathVariable("productId") String productId) {

		Map<String, String> mapData = Maps.newHashMap();
		String userId = VariableUtils.typeCast(SessionVariable.getCurrentSessionVariable().getUser().get("id"),
				String.class);
		boolean isAlreayAdded = buyCardService.isAddedIntoBuyCard(userId, productId);
		if (isAlreayAdded) {
			mapData.put("msg", "had");
			return mapData;
		}
		buyCardService.putProductIntoBag(userId, productId);
		mapData.put("msg", "sccuess");
		return mapData;
	}

	@RequestMapping(value = "removeOneProductFromBag/{productId}", method = RequestMethod.GET)
	public String removeOneProductFromBag(@PathVariable("productId") String productId) {

		String userId = VariableUtils.typeCast(SessionVariable.getCurrentSessionVariable().getUser().get("id"),
				String.class);

		buyCardService.removeProductListFromBuyCard(userId, productId);
		return "redirect:/order/buycard/list";
	}

	@RequestMapping(value = "batchremove", method = RequestMethod.POST)
	public String removeBatchProductFromBag(@RequestParam List<String> productIds, RedirectAttributes redirectAttributes) {

		String userId = VariableUtils.typeCast(SessionVariable.getCurrentSessionVariable().getUser().get("id"),
				String.class);

		if (productIds == null || productIds.size() == 0) {
			return "redirect:/order/buycard/list";
		}
		String[] array = productIds.toArray(new String[productIds.size()]);
		buyCardService.removeProductListFromBuyCard(userId, array);
		return "redirect:/order/buycard/list";
	}

	@RequestMapping(value = "get/productconut", method = RequestMethod.POST)
	public @ResponseBody Object getProductCountInBag() {
		String userId = VariableUtils.typeCast(SessionVariable.getCurrentSessionVariable().getUser().get("id"),
				String.class);
		return buyCardService.countProductInBagAboutBuyCard(userId);
	}

	@RequestMapping(value = "list", method = RequestMethod.GET)
	public void getProductList(Model model) {
		String userId = VariableUtils.typeCast(SessionVariable.getCurrentSessionVariable().getUser().get("id"),
				String.class);

		model.addAttribute("customerOrderingList", buyCardService.getProductListFromBag(userId));
		model.addAttribute("addressList", addressService.queryList(userId));
	}

}
