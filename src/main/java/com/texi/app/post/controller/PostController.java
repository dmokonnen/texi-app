package com.texi.app.post.controller;

import com.texi.app.core.Response;
import com.texi.app.core.ResponseBuilder;
import com.texi.app.domain.*;
import com.texi.app.post.service.PostService;
import com.texi.app.user.service.UserServices;
import com.texi.app.utility.Producer;
import com.texi.app.utility.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/posts")
@SessionAttributes({"user", "wtf", "friends"})
public class PostController {

    private final PostService postService;
    private final UserServices userService;
    private final ResponseBuilder responseBuilder;
    private final Upload upload;
    private final Producer producer;

    @Autowired
    public PostController(PostService postService, UserServices userService, ResponseBuilder responseBuilder, Upload upload, Producer producer) {
        this.postService = postService;
        this.userService = userService;
        this.responseBuilder = responseBuilder;
        this.upload = upload;
        this.producer = producer;
    }

    @GetMapping("/")
    public @ResponseBody
    Response index() { // no use case for this though
        List<Post> postList = postService.findAll();
        return responseBuilder.buildSuccess(postList);
    }

    @PostMapping("/add")
    public RedirectView add(@RequestParam("title") String title, @RequestParam("image") MultipartFile image,
                            @RequestParam("video") MultipartFile video, Principal principal,
                            @RequestParam("type") String type, @RequestParam(value = "start", required = false) String start,
                            @RequestParam(value = "end", required = false) String end,
                            @RequestParam(value = "end", required = false) String notify) {
        if (title.equals("") || image.isEmpty() && video.isEmpty()) {
            System.out.println("Blank post received, ignoring...");
            return new RedirectView("/user/dashboard");
        }
        Post post;
        if (type.equals("post")) {
            post = new Post();
        } else {
            Advert advert = new Advert();
            advert.setStart(LocalDate.parse(start));
            advert.setStop(LocalDate.parse(end));
            post = advert;
        }
        post.setTitle(title);
        post.setDescription(title);
        post.setStatus(Status.ACTIVE);

        if (principal == null) return new RedirectView("/auth");
        User user = userService.findByUsername(principal.getName());

        // save to DB, and handle the rest asynchronously
        post = postService.save(post, user);

        PostData postData = new PostData(
                post,
                image.isEmpty() ? null : image,
                video.isEmpty() ? null : video,
                !notify.isEmpty()
        );
        Payload<PostData> postDataPayload = new Payload<>(postData);
        producer.produce(postDataPayload);

        return new RedirectView("/user/dashboard");
    }

    @PostMapping("/getAll/{userId}")
    public @ResponseBody
    String getByUser(@PathVariable String userId, Principal principal) {
        if (principal == null) return "redirect:/auth";
        User user = userService.findByUsername(principal.getName());
        List<Post> postList = postService.findByUser(user);
        return "/user-profile"; // todo create view template 'user-profile'
    }

    @PostMapping("/delete/{postId}")
    public @ResponseBody
    Response delete(@PathVariable String postId) {
        postService.delete(Long.parseLong(postId));
        return responseBuilder.buildSuccess();
    }

    @GetMapping("/unhealthy")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER')")
    public String unhealthyPosts(Model model, HttpSession session) {
        System.out.println("unhealthy");
        List<Post> posts = postService.getUnhealthyPosts();
        model.addAttribute("unhealthy", posts);
        session.setAttribute("unhealthy", posts);
        return "unhealthy-posts";
    }

    @GetMapping("/enable")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPER')")
    public String enablePosts(@RequestParam("p") Long id, Model model) {
        postService.enablePost(id);
        return "unhealthy-posts";
    }

}
