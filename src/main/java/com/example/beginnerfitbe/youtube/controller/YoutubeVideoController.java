package com.example.beginnerfitbe.youtube.controller;

import com.example.beginnerfitbe.jwt.util.JwtUtil;
import com.example.beginnerfitbe.playlist.service.PlaylistService;
import com.example.beginnerfitbe.youtube.dto.YoutubeVideoDto;
import com.example.beginnerfitbe.youtube.service.YoutubeVideoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeVideoService youtubeVideoService;
    private final PlaylistService playlistService;
    private final JwtUtil jwtUtil;

    //비디오 선택 ->  시청 true -> 다 true면 플레이리스트 완료 true
    @PostMapping("/videos/{videoId}")
    @Operation(summary = "비디오 선택 메소드", description = "비디오를 선택하면 시청 한 상태로 업데이트 합니다.")
    private ResponseEntity<?> watchVideo(HttpServletRequest request, @PathVariable Long videoId) {
        Long userId = jwtUtil.getUserId(jwtUtil.resolveToken(request).substring(7));

        YoutubeVideoDto youtubeVideoDto = youtubeVideoService.watchVideo(userId, videoId);

        Long playlistId = youtubeVideoDto.getPlaylistId();
        playlistService.update(playlistId);

        return ResponseEntity.ok("비디오 시청 상태가 업데이트되었습니다.");
    }

    @GetMapping("/videos")
    @Operation(summary = "비디오 전체 조회 메소드", description = "전체 비디오를 조회합니다.")
    private ResponseEntity<?> list(){
        return ResponseEntity.ok(youtubeVideoService.list());
    }

    @GetMapping("/videos/{videoId}")
    @Operation(summary = "비디오 상세 조회 메소드", description = "비디오의 상세 정보를 조회합니다.")
    private ResponseEntity<?> read(@PathVariable Long videoId){
        return ResponseEntity.ok(youtubeVideoService.read(videoId));
    }

    @GetMapping("/{playlistId}/videos")
    @Operation(summary = "플레이리스트 별 비디오 조회", description = "플레이리스트에 해당하는 비디오를 조회합니다.")
    private ResponseEntity<?> getYoutubeVideosByPlaylist(@PathVariable Long playlistId){
        return ResponseEntity.ok(youtubeVideoService.getYoutubeVideosByPlaylist(playlistId));
    }

    @GetMapping("/videos/watched")
    @Operation(summary = "시청한 비디오 목록 조회", description = "사용자가 시청했던 비디오 목록을 조회합니다.")
    public ResponseEntity<?> getWatchedVideo(HttpServletRequest request) {
        Long userId = jwtUtil.getUserId(jwtUtil.resolveToken(request).substring(7));
        return ResponseEntity.ok(youtubeVideoService.getWatchedVideo(userId));
    }

    @GetMapping("/videos/recent-watched")
    @Operation(summary = "시청한 비디오 목록 3개 조회", description = "사용자가 시청했던 비디오 목록 중 최신 순으로 3개 조회합니다.")
    public ResponseEntity<?> getRecentWatchedVideo(HttpServletRequest request) {
        Long userId = jwtUtil.getUserId(jwtUtil.resolveToken(request).substring(7));
        return ResponseEntity.ok(youtubeVideoService.getRecentWatchedVideo(userId));
    }

    //홈화면 다음 비디오 재생
    @GetMapping("/videos/next")
    @Operation(summary = "다음 비디오 조회", description = "사용자가 마지막으로 시청한 다음 비디오 정보를 조회합니다.")
    public ResponseEntity<?> getRecentVideo(HttpServletRequest request) {
        Long userId = jwtUtil.getUserId(jwtUtil.resolveToken(request).substring(7));
        return ResponseEntity.ok(youtubeVideoService.getNextVideo(userId));
    }

}
