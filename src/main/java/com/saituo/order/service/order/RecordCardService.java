package com.saituo.order.service.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.saituo.order.commons.VariableUtils;
import com.saituo.order.dao.order.AgentDao;
import com.saituo.order.dao.order.ProductDao;
import com.saituo.order.entity.order.Agent;
import com.saituo.order.entity.order.CustomerOrdering;
import com.saituo.order.entity.order.Product;
import com.saituo.order.service.cache.RedisCacheService;

@Service
public class RecordCardService {

	@Autowired
	private RedisCacheService redisCacheService;

	@Autowired
	private ProductDao productDao;

	@Autowired
	private AgentDao agentDao;

	private static final String ADD_BAG_FOR_RECORD_CACHE_KEY = "addedrecord";

	/**
	 * 加入购物车
	 * 
	 * @param userId
	 * @param productId
	 */
	public void putProductIntoBag(String userId, String productId) {
		redisCacheService.putProductIntoBagAboutBuyCard(ADD_BAG_FOR_RECORD_CACHE_KEY, userId, productId, "1");
	}

	/**
	 * 删除购物车中的产品
	 * 
	 * @param userId
	 * @param productIds
	 */
	public void removeProductListFromBuyCard(String userId, String... productIds) {
		redisCacheService.removeProductListFromBuyCard(ADD_BAG_FOR_RECORD_CACHE_KEY, userId, productIds);
	}

	/**
	 * 统计购物车中的产品数量
	 * 
	 * @param userId
	 * @return
	 */
	public Long countProductInBagAboutBuyCard(String userId) {
		return redisCacheService.countProductInBagAboutBuyCard(ADD_BAG_FOR_RECORD_CACHE_KEY, userId);
	}

	/**
	 * 针对某一个用户的购物车中的数量
	 * 
	 * @param userId
	 * @return
	 */
	public Long getBuyProductCount(String userId) {
		return redisCacheService.countProductInBagAboutBuyCard(ADD_BAG_FOR_RECORD_CACHE_KEY, userId);
	}

	/**
	 * 是否已经添加到购物车中
	 * 
	 * @param userId
	 * @param productId
	 * @return
	 */
	public boolean isAddedIntoBuyCard(String userId, String productId) {
		return redisCacheService.isAddedIntoBuyCard(ADD_BAG_FOR_RECORD_CACHE_KEY, userId, productId);
	}

	/**
	 * 获取购物车中的产品列表
	 */

	public List<CustomerOrdering> getProductListFromBag(String userId) {

		List<CustomerOrdering> result = new ArrayList<CustomerOrdering>();
		Map<Object, Object> mapData = redisCacheService.getProductIdAndBuyNumMapFromCache(ADD_BAG_FOR_RECORD_CACHE_KEY,
				userId);

		if (mapData == null || mapData.size() == 0) {
			return result;
		}

		List<String> productIds = Lists.newArrayList();
		for (Entry<Object, Object> entry : mapData.entrySet()) {
			String productId = String.valueOf(entry.getKey());
			productIds.add(productId);
		}
		List<Product> productList = productDao.getProductListByProductIds(productIds);
		try {
			for (Product product : productList) {
				CustomerOrdering customerOrdering = new CustomerOrdering();
				BeanUtils.copyProperties(customerOrdering, product);
				customerOrdering.setSubscriptCount(VariableUtils.typeCast(
						mapData.get(String.valueOf(product.getProductId())), Integer.class));
				result.add(customerOrdering);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Map<String, String> getAgentList(boolean forValidFlag) {
		Map<String, String> filter = Maps.newHashMap();
		if (forValidFlag) {
			filter.put("delFlag", "0");
		}
		List<Agent> agentList = agentDao.getAgentListByIdAndDelFlag(filter);
		Map<String, String> result = Maps.newHashMap();
		for (Agent agent : agentList) {
			result.put(String.valueOf(agent.getAgentId()), agent.getAgentName());
		}
		return result;
	}

}
