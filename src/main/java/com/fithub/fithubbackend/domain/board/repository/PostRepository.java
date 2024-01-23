package com.fithub.fithubbackend.domain.board.repository;

import com.fithub.fithubbackend.domain.board.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT post " +
            "FROM Post post " +
            "JOIN FETCH post.user user " +
            "JOIN FETCH user.profileImg profileImg " +
            "JOIN FETCH post.postDocuments postDocuments " +
            "WHERE post.id = :postId")
    Post findByPostIdWithFetchJoin(@Param("postId") long postId);

}
