package com.texi.app.post.service;

import com.texi.app.domain.Post;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService implements IPostService {
    @Override
    public Post save(Post post) {
        return null;
    }

    @Override
    public List<Post> findAll() {
        return null;
    }

    @Override
    public Post findById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
