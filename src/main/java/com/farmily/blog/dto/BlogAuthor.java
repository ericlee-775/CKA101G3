package com.farmily.blog.dto;

public class BlogAuthor {

    private final Integer userId;    // 會員作者
    private final Integer farmerId;  // 小農作者

    private BlogAuthor(Integer userId, Integer farmerId) {
        this.userId = userId;
        this.farmerId = farmerId;
    }

    //看出是哪種作者
    public static BlogAuthor ofFarmer(Integer farmerId) {
        return new BlogAuthor(null, farmerId);
    }
    public static BlogAuthor ofMember(Integer userId) {
        return new BlogAuthor(userId, null);
    }

    public Integer getUserId()   { return userId; }
    public Integer getFarmerId() { return farmerId; }

    public boolean isFarmer() { return farmerId != null; }
}
