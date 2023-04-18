package com.instagram.instagram.service;

import com.instagram.instagram.domains.HashTag;
import com.instagram.instagram.domains.auth.AuthUser;
import com.instagram.instagram.domains.basic.Document;
import com.instagram.instagram.domains.basic.Post;
import com.instagram.instagram.dto.CreatePostDTO;
import com.instagram.instagram.dto.PostDto;
import com.instagram.instagram.firebase.MediaService;
import com.instagram.instagram.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.instagram.instagram.mapper.PostMapper.POST_MAPPER;
import static java.util.UUID.randomUUID;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final DocumentService documentService;
    private final MediaService mediaService;
    private final UserService userService;
    private final HashTagService hashTagService;


    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPost(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getPostsByUserId(Long id) {
        return postRepository.findAllByUserId(id);
    }

    public Page<Post> getPostsByUserIdWithPagination(Pageable pageable, Long id) {
        return postRepository.findAllByUserIdWithPagination(pageable, id);

    }

    public Page<Post> getPostsWithPagination(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post save(PostDto dto) {
        CreatePostDTO createPostDTO = new CreatePostDTO(dto.getCreatedBy(),dto.getCaption(),dto.getLocation());
        Post post = POST_MAPPER.toEntity(createPostDTO);
        List<Document> documents = new ArrayList<>();
        dto.getDocuments().forEach(document -> {
            documents.add(documentService.getDocument(document).orElseThrow(
                    () -> new RuntimeException("Document not found")
            ));
        });

        List<AuthUser> mentions = new ArrayList<>();
        dto.getMentions().forEach(mention -> mentions.add(userService.getUser(mention)));

        List<HashTag> hashTags = new ArrayList<>();
        dto.getHashTags().forEach(hashTag -> {
            hashTagService.getHashTag(hashTag).ifPresentOrElse(
                    hashTags::add,
                    () -> hashTags.add(hashTagService.save(hashTag))
            );
        });

        post.setCreatedBy(dto.getCreatedBy());
        post.setDocuments(documents);
        post.setMentions(mentions);
        post.setHashTags(hashTags);

        return postRepository.save(post);
    }
}

