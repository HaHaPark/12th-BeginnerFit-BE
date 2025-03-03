package com.example.beginnerfitbe.scrap.service;

import com.example.beginnerfitbe.error.StateResponse;
import com.example.beginnerfitbe.post.domain.Post;
import com.example.beginnerfitbe.post.repository.PostRepository;
import com.example.beginnerfitbe.scrap.domain.Scrap;
import com.example.beginnerfitbe.scrap.dto.ScrapDto;
import com.example.beginnerfitbe.scrap.repository.ScrapRepository;
import com.example.beginnerfitbe.user.domain.User;
import com.example.beginnerfitbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public StateResponse create(Long userId, Long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found user"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("not found post"));

        Optional <Scrap> scrapOpt = scrapRepository.findByUserAndPost(user, post);

        if(scrapOpt.isPresent()){
            return StateResponse.builder()
                    .code("FAIL")
                    .message("이미 스크랩 했습니다.")
                    .build();
        }
        Scrap scrap = Scrap.builder()
                .user(user)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();

        scrapRepository.save(scrap);
        return StateResponse.builder()
                .code("SUCCESS")
                .message("게시글을 성공적으로 스크랩했습니다.")
                .build();
    }

    public List<ScrapDto> list(){
        return scrapRepository.findAll().stream()
                .map(ScrapDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ScrapDto> getScrapsByUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found user"));
        return scrapRepository.findScrapsByUser(user).stream()
                .map(ScrapDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ScrapDto> getScrapsByPost(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("not found post"));
        return scrapRepository.findScrapsByPost(post).stream()
                .map(ScrapDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Boolean checkScrap(Long userId, Long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found user"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("not found post"));

        Optional<Scrap> scrapOpt = scrapRepository.findByUserAndPost(user, post);

        return scrapOpt.isPresent();
    }

    public StateResponse delete(Long userId, Long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("not found user"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("not found post"));
        Optional<Scrap> scrapOpt = scrapRepository.findByUserAndPost(user, post);

        if(scrapOpt.isPresent()){
            scrapRepository.delete(scrapOpt.get());
            return StateResponse.builder()
                    .code("SUCCESS")
                    .message("스크랩을 취소했습니다.")
                    .build();
        }
        return StateResponse.builder()
                .code("FAIL")
                .message("스크랩 내역이 없습니다.")
                .build();

    }

}
