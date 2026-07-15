package com.farmily.ai.memory;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Repository
public class RedisChatMemoryRepository implements ChatMemoryRepository {
	 private static final String KEY_PREFIX = "farmily:chat:";
	 
	 private static final int TTL_SECONDS = 60*60*24;
	 @Autowired
	 private JedisPool jedisPool;
	 private String buildKey(String conversationId) {
	        return KEY_PREFIX + conversationId;
	    }
	 //好累~~
	 //列出所有對話
	 @Override
	 public List<String> findConversationIds() {
		try(Jedis jedis = jedisPool.getResource()){
			return jedis.keys(KEY_PREFIX +"*").stream()
					.map(k->k.substring(KEY_PREFIX.length()))
					.toList();

		}
		
	 }
	 //讀出歷史變成List
	 @Override
	 public List<Message> findByConversationId(String conversationId) {
		String json;
		try(Jedis jedis = jedisPool.getResource()){
			json = jedis.get(buildKey(conversationId));
		}
		List<Message> message = new ArrayList<>();
		if(json==null) {
			return message;
		}
		JSONArray arr =new JSONArray(json);
		for(int i=0;i<arr.length();i++) {
			JSONObject o = arr.getJSONObject(i);
			message.add(toMessage(o.getString("type"),o.getString("text")));
		}
		return message;
	 }
	 @Override
	 public void saveAll(String conversationId, List<Message> messages) {
		 JSONArray arr = new JSONArray();
		 for(Message m : messages) {
			 JSONObject o = new JSONObject();
			 o.put("type", m.getMessageType().getValue());
			 o.put("text", m.getText());
			 arr.put(o);
		 }
		try(Jedis jedis =jedisPool.getResource() ){
			String key = buildKey(conversationId);
			jedis.set(key,arr.toString());
			jedis.expire(key,TTL_SECONDS);//刷新24小時喔~~~
		}
	 }
	 @Override
	 public void deleteByConversationId(String conversationId) {
		try(Jedis jedis = jedisPool.getResource()){
			jedis.del(buildKey(conversationId));
		}
	 }
	 
	// 自訂反序列化方法（直接對字串判斷，不用 MessageType）
	 private Message toMessage(String type, String text) {
	     return switch (type) {
	         case "assistant" -> new AssistantMessage(text);
	         case "system"    -> new SystemMessage(text);
	         default          -> new UserMessage(text);   // "user" 及其他
	     };
	 }
	 
    
}