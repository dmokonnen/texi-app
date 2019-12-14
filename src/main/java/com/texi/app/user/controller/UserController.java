package com.texi.app.user.controller;

import com.texi.app.core.Response;
import com.texi.app.core.ResponseBuilder;
import com.texi.app.core.ResponseCode;
import com.texi.app.domain.Post;
import com.texi.app.domain.User;
import com.texi.app.post.service.PostService;
import com.texi.app.security.UserDetailsServiceImpl;
import com.texi.app.user.service.UserServices;
import com.texi.app.utility.Upload;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    public static String uploadDirectory = System.getProperty("user.dir")+"/photoUploads";

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserServices services;

    @Autowired
    private PostService postService;

    @Autowired
    private Upload upload;

    @Autowired
    private ResponseBuilder responseBuilder;

    @RequestMapping("/")
    public String index(Model model){
        model.addAttribute("name", "James Bond");
        model.addAttribute("message", "Hello World");
        model.addAttribute("tasks", "Digging");
        return "index";
    }

    // @todo extract user from principle object once implemented
    @ApiOperation(value = "Follow User")
    @RequestMapping(value = "/{me}/follow/{id}")
    public @ResponseBody Response follow(@PathVariable("id") String id, @PathVariable("me") String me){
        Response response = services.getUser( Long.parseLong(me));
        if(response.getCode() != ResponseCode.SUCCESS.getCode())
            return response;

        return services.follow((User) response.getData(), Long.parseLong(id));
    }

    @ApiOperation(value = "Follow User")
    @RequestMapping(value = "/follow")
    public String followNew(@RequestParam("f") String id, Model model){
        User user = (User) model.getAttribute("user");
        services.follow(user, Long.parseLong(id));
        return "redirect:dashboard";
    }

    @ApiOperation(value = "Un follow User")
    @RequestMapping(value = "/unfollow")
    public String unfollow(@RequestParam("f") String username, Model model){
        System.out.println(".......here.......");
        User user = (User) model.getAttribute("user");
        services.unfollow(user, username);
        return "redirect:dashboard";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, RedirectAttributes ra,
                         @RequestParam("photo") MultipartFile photo, Model model){

        if (bindingResult.hasErrors()){
            return "login";
        }

        User u = services.findByUsername(user.getUsername());

        if (u != null) {
            bindingResult
                    .rejectValue("username", "error.user",
                            "There is already a user registered with the username provided");
            return "login";
        }

        String fileNameAndPath = upload.upload(photo);
        user.setPhotoUrl(fileNameAndPath);
        Response res = services.save(user);

        ra.addFlashAttribute("user",res.getData());
        model.addAttribute("user", res.getData());
        return "redirect:auth";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("user") User user,
                         BindingResult bindingResult, Principal principal, Model model){
        if (principal == null) return "redirect:auth";
        if (bindingResult.hasErrors()) {
            return "manage-profile";
        }

        // Assume it is the user logged in who is updating their info
        User u = services.findByUsername(user.getUsername());
        if (u != null && !user.getUsername().equals(principal.getName())) { // validate the new username
            bindingResult.rejectValue("username", "error.user", "There is already a user registered with the username provided");
            return "redirect:auth";
        }
        u = services.findByUsername(principal.getName());
        u.setFirstName(user.getFirstName());
        u.setLastName(user.getLastName());
        u.setUsername(user.getUsername());
        u.setBirthday(user.getBirthday());
        u.setPassword(user.getPassword());
        services.save(u);
        model.addAttribute("status", "Success");
        return "redirect:manage-profile";
    }

    @GetMapping(value = {"/dashboard"})
    public String dashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:auth";
        }
        System.out.println("Principle: "+principal.getName());

        User u = services.findByUsername(principal.getName());
        model.addAttribute("user", u);

        List<User> wtf = services.whoToFollow(u);
        model.addAttribute("wtf", wtf);
        model.addAttribute("friends", u.getFollowing());

        List<Post> postList = postService.getPostsForUser(u);
        model.addAttribute("posts", postList);

        return "dashboard";
    }

    @GetMapping("/auth")
    public String auth(@ModelAttribute User user) {
        return "login";
    }

    @RequestMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @ModelAttribute User user, Model model, Principal principal) {
        System.out.println("Login.....");
        String errorMessge = null;
        if (error != null) {
            errorMessge = "Username or Password is incorrect !!";
            System.out.println("Principle: login "+principal.getName());
            return "dashboard";
        }
        if (logout != null) {
            errorMessge = "You have been successfully logged out !!";
        }
        model.addAttribute("errorMessge", errorMessge);
        return "login";
    }

    @GetMapping("/manage-profile")
    public String manageProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:auth";

        User u = services.findByUsername(principal.getName());
        model.addAttribute("user", u);

        List<User> wtf = services.whoToFollow(u);
        model.addAttribute("wtf", wtf);
        model.addAttribute("friends", u.getFollowing());

        return "manage-profile";
    }

    @GetMapping("/timeline")
    public String timeline(Model model, Principal principal) {
        if (principal == null) return "redirect:auth";

        User u = services.findByUsername(principal.getName());
        model.addAttribute("user", u);

        List<User> wtf = services.whoToFollow(u);
        model.addAttribute("wtf", wtf);
        model.addAttribute("friends", u.getFollowing());

        List<Post> postList = postService.findByUser(u);
        model.addAttribute("posts", postList);

        return "timeline";
    }

}
