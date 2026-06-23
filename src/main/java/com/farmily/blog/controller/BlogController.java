package com.farmily.blog.controller;

import com.farmily.blog.dto.BlogQueryParms;
import com.farmily.blog.dto.BlogRequest;
import com.farmily.blog.dto.BlogTypeResponse;
import com.farmily.blog.model.Blog;
import com.farmily.blog.service.BlogService;
import com.farmily.blog.util.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")   // class 層級前綴：底下的 /blogs 實際路徑就是 /api/blogs
public class BlogController {

    @Autowired
    private BlogService blogService;

    /* ===== 公開 ===== */

    @GetMapping("/blogs/types")
    public List<BlogTypeResponse> types() {return blogService.listTypes();}

    @GetMapping("/blogs")
    public ResponseEntity<Page<Blog>> getBlogAll(
            //查詢條件
            @RequestParam(required = false) Integer blogTypeId,
            @RequestParam(required = false) String search,
            //排序
            @RequestParam(defaultValue = "blog_time") String orderBy,
            @RequestParam(defaultValue = "desc") String sort,
            //分頁
            @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset
    ) {
        BlogQueryParms blogQueryParms = new BlogQueryParms();
        blogQueryParms.setBlogTypeId(blogTypeId);
        blogQueryParms.setSearch(search);
        blogQueryParms.setOrderBy(orderBy);
        blogQueryParms.setSort(sort);
        blogQueryParms.setLimit(limit);
        blogQueryParms.setOffset(offset);

        List<Blog> blogList = blogService.getBlogs(blogQueryParms);

        Integer total = blogService.countBlogs(blogQueryParms);

        Page<Blog> page = new Page<>();
        page.setLimit(limit);
        page.setOffset(offset);
        page.setTotal(total);
        page.setResults(blogList);

        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @GetMapping("/blogs/{blogId}")
    public ResponseEntity<Blog> getBlogOne(@PathVariable Integer blogId) {
        //從service 去抓blogId
        Blog blog = blogService.getBlogById(blogId);

        if (blog != null) {
            return ResponseEntity.status(HttpStatus.OK).body(blog);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    /* ===== 寫作(會員) ===== */

    @PostMapping("/blogs/")
    public ResponseEntity<Blog> createBlog(@RequestParam @Valid BlogRequest blogRequest) {

       Integer BlogId = blogService.createBlog(blogRequest);

        Blog newBlog = blogService.getBlogById(BlogId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newBlog);


    }

    @PutMapping("/blogs/{blogId}")
    public ResponseEntity<Blog> updateBlog(@PathVariable @RequestParam Integer blogId, @RequestParam @Valid BlogRequest blogRequest) {
       Blog blog = blogService.getBlogById(blogId);

       if(blog == null) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       }

       blogService.updateBlog(blogId, blogRequest);

        Blog updateBlog = blogService.getBlogById(blogId);

        return ResponseEntity.status(HttpStatus.OK).body(updateBlog);

    }

    @DeleteMapping("blogs/{blogId}")
    public ResponseEntity<?> deleteBlog(@PathVariable Integer blogId) {
        blogService.deleteBlog(blogId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); //告訴前端刪除的東西已經不存在


    }





    /* ===== 互動 ===== */





}
