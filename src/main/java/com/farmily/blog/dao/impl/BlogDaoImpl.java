package com.farmily.blog.dao.impl;

import com.farmily.blog.dao.BlogDao;
import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.rowmapper.BlogRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlogDaoImpl implements BlogDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /* ===== 公開 ===== */

    @Override
    public List<BlogTypeResponse> listTypes() {
        String sql = "SELECT blog_type_id, blog_type_name, blog_type_text, blog_type_img FROM blog_type ORDER BY blog_type_id";

        Map<String, Object> map = new HashMap<>();
        return namedParameterJdbcTemplate.query(sql, map , (rs, rowNum) -> {
            BlogTypeResponse blogTypeResponse = new BlogTypeResponse();

            blogTypeResponse.setBlogTypeId(rs.getInt("blog_type_id"));
            blogTypeResponse.setBlogTypeName(rs.getString("blog_type_name"));
            blogTypeResponse.setBlogTypeText(rs.getString("blog_type_text"));
            blogTypeResponse.setBlogTypeImg(rs.getBytes("blog_type_img"));

            return blogTypeResponse;
        });
    }

    @Override
    public List<Blog> getAll() {
        String sql = "SELECT blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, blog_content, " +
                "blog_img, blog_like_count, blog_time, blog_status FROM Blog WHERE 1=1";

        Map<String, Object> map = new HashMap<>();


        List<Blog> blogList = namedParameterJdbcTemplate.query(sql,map,new BlogRowMapper() );

        return blogList;
    }

    @Override
    public Blog getBlogById(Integer blogId) {
        String sql = "SELECT blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, " +
                "blog_content, blog_img, blog_like_count, " +
                "blog_time, blog_status FROM Blog WHERE blog_id = :blogId" ;

        Map<String, Object> map = new HashMap<>();
        map.put("blogId", blogId);

        //結果用blogRowMapper 轉成 List<Blog>
        List<Blog> blogList = namedParameterJdbcTemplate.query(sql, map, new BlogRowMapper());
        //query執行三個參數 sql：要執行的SQL。 map：SQL裡參數的值。new BlogRowMapper()：把每一列轉成Product物件的工具

        if (blogList.size() > 0) {
            //回傳第一筆
            return blogList.get(0);
        }else {
            return null;
        }

    }

    @Override
    public List<Blog> getBlogs(BlogQueryParms blogQueryParms) {
        String sql = "SELECT blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, blog_content, " +
                "blog_img, blog_like_count, blog_time, blog_status FROM Blog WHERE 1=1";

        Map<String, Object> map = new HashMap<>();
        // 把篩選條件（分類、關鍵字）動態拼進 SQL，沒帶的條件就不加
        sql = addFilter(sql, map, blogQueryParms);

        // 排序：欄位與方向都先做白名單，避免使用者亂塞字串造成 SQL injection
        sql = sql + " ORDER BY " + safeOrderBy(blogQueryParms.getOrderBy())
                + " " + safeSort(blogQueryParms.getSort());

        // 分頁
        sql = sql + " LIMIT :limit OFFSET :offset";
        map.put("limit", blogQueryParms.getLimit());
        map.put("offset", blogQueryParms.getOffset());

        return namedParameterJdbcTemplate.query(sql, map, new BlogRowMapper());
    }

    @Override
    public Integer countBlogs(BlogQueryParms blogQueryParms) {
        String sql = "SELECT COUNT(*) FROM Blog WHERE 1=1";

        Map<String, Object> map = new HashMap<>();
        // 用跟 getBlogs 一樣的篩選條件，但不加排序與分頁
        sql = addFilter(sql, map, blogQueryParms);

        return namedParameterJdbcTemplate.queryForObject(sql, map, Integer.class);
    }
    /* ===== 寫作(會員) ===== */

    @Override
    public Integer createBlog(BlogRequest blogRequest) {
        String sql = "INSERT INTO blog (blog_title, user_id,farmer_id, blog_type_id, blog_content, blog_img) VALUES (:blogTitle, :userId, :farmerId, :blogTypeId, :blogContent, :blogImg)";

        Map<String, Object> map = new HashMap<>();
        map.put("blogTitle", blogRequest.getBlogTitle());
        map.put("userId", blogRequest.getUserId());
        map.put("farmerId", blogRequest.getFarmerId());
        map.put("blogTypeId", blogRequest.getBlogTypeId());
        map.put("blogContent", blogRequest.getBlogContent());
        map.put("blogImg", blogRequest.getBlogImg());



        int result = namedParameterJdbcTemplate.update(sql, map);
        return 0;
    }

    @Override
    public void updateBlog(Integer blogId, BlogRequest blogRequest) {

    }

    @Override
    public void deleteBlog(Integer blogId) {

    }



    /* ===== 方法 ===== */

    // 共用的篩選條件，getBlogs 與 countBlogs 都呼叫，確保兩邊條件一致
    private String addFilter(String sql, Map<String, Object> map, BlogQueryParms blogQueryParms) {
        if (blogQueryParms.getBlogTypeId() != null) {
            sql = sql + " AND blog_type_id = :blogTypeId";
            map.put("blogTypeId", blogQueryParms.getBlogTypeId());
        }
        if (blogQueryParms.getSearch() != null && !blogQueryParms.getSearch().isBlank()) {
            sql = sql + " AND (blog_title LIKE :search OR blog_content LIKE :search)";
            map.put("search", "%" + blogQueryParms.getSearch() + "%");
        }
        return sql;
    }

    // 只允許白名單內的欄位排序，預設 blog_time
    private String safeOrderBy(String orderBy) {
        if ("blog_like_count".equals(orderBy)) {
            return "blog_like_count";
        }
        return "blog_time";
    }

    // 只允許 asc / desc，預設 desc
    private String safeSort(String sort) {
        return "asc".equalsIgnoreCase(sort) ? "ASC" : "DESC";
    }
}
