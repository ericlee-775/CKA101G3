package com.farmily.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

@Service
public class ProductClickService {

	private static final String CLICKS_KEY = "farmily:product:clicks";
	private static final String HOT_KEY = "farmily:product:hot";
	private static final int HOT_SIZE = 4;

	private final JedisPool jedisPool;

	public ProductClickService(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	// 商品編號+點擊次數
	public void addClick(Integer productId) {

		try (Jedis jedis = jedisPool.getResource()) {

			jedis.zincrby(CLICKS_KEY, 1, String.valueOf(productId));
		} catch (JedisException e) {
			System.err.println("熱門商品點擊統計失敗:" + e.getMessage());

		}
	}

	// 取出點擊最高的前 N 名商品 id
	public List<Integer> getTopProductIds(int limit) {

		List<Integer> topIds = new ArrayList<>();
		try (Jedis jedis = jedisPool.getResource()) {

			List<String> ids = jedis.zrevrange(CLICKS_KEY, 0, limit - 1);
			for (String s : ids) {
				topIds.add(Integer.valueOf(s));
			}
		} catch (JedisException e) {
			System.err.println("取得熱門商品失敗:" + e.getMessage());
		}

		return topIds;

	}

	// 從左到右：秒 分 時 日 月 星期
	@Scheduled(cron = "0 0 3 * * MON")
	public void settleHotProductIds() {
		List<Integer> topIds = getTopProductIds(HOT_SIZE);
		if (topIds.isEmpty()) {
			return;
		}
		Jedis jedis = jedisPool.getResource();
		try {

			jedis.del(HOT_KEY);
			for (Integer s : topIds) {
				jedis.rpush(HOT_KEY, String.valueOf(s));

			}
			jedis.del(CLICKS_KEY);
		} finally {

			jedis.close();
		}

	}

	public List<Integer> getHotProductIds() {
		
		List<Integer> hotIds = new ArrayList<>();
		try (Jedis jedis = jedisPool.getResource()) {
			List<String> ids = jedis.lrange(HOT_KEY, 0, -1);
			for (String s : ids) {
				hotIds.add(Integer.valueOf(s));
			}
		} catch (JedisException e) {
			System.err.println("取得熱門商品列表失敗:" + e.getMessage());
		}
		return hotIds;

	}

}
