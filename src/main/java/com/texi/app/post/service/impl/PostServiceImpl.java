package com.texi.app.post.service.impl;

import com.texi.app.domain.Post;
import com.texi.app.domain.User;
import com.texi.app.post.repository.PostRepository;
import com.texi.app.post.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void save(Post post) {
        postRepository.save(post);
    }

    @Override
    public Post findById(Long postId) {
        return postRepository.findById(postId).orElseGet(null);
    }

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        Post post = findById(id);
        postRepository.delete(post);
    }

    @Override
    public List<Post> findAllByDescriptionOrByTitleOrByUser_UsernameOrUser_LastNameOrUser_FirstNameAllIgnoreCaseContainsOrderByDateDesc(String description, String title, String username, String lName, String fName) {
        return postRepository.findAllByDescriptionOrByTitleOrByUser_UsernameOrUser_LastNameOrUser_FirstNameAllIgnoreCaseContainsOrderByDateDesc(description, title, username, lName, fName);
    }

    @Override
    public List<Post> findFollowingsPost(Integer id, String title, String descrtiption, String fName, String lName, String uName, String fLName, String FfName, String fUName) {
        return postRepository.findFollowingsPost(id, title, descrtiption, fName, lName, uName, fLName, FfName, fUName);
    }


}
