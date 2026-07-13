package com.farmily.groupbuy.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupBuyWishListRepository extends JpaRepository<GroupBuyWishListVO, GroupBuyWishListId> {
boolean existsByUser_UserIdAndGroupBuy_GroupBuyId(Integer userId,Integer groupBuyId);//判斷有無存在過，以防重複存入
Optional<GroupBuyWishListVO>findByUser_UserIdAndGroupBuy_GroupBuyId(Integer userId,Integer groupBuyId);//查看單一的團購最愛
List<GroupBuyWishListVO>
findByUser_UserIdAndGroupBuy_StatusOrderBySavedDatetimeDesc(Integer userId,GroupBuyStatus status);//按照加入時間排序，最新的在最上面
void deleteByUser_UserIdAndGroupBuy_GroupBuyId(Integer userId,Integer groupBuyId);//移除這筆收藏



}
