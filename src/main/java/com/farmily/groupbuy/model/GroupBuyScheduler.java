package com.farmily.groupbuy.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.farmily.groupbuy.service.GroupBuyService;

public class GroupBuyScheduler {

	@Autowired
	private GroupBuyService groupBuyService;

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Taipei")
    public void checkExpiredGroupBuys() {
        groupBuyService.checkExpiredGroupBuys();
    }

}
