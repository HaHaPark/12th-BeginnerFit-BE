package com.example.beginnerfitbe.user.service;

import com.example.beginnerfitbe.attendance.domain.Attendance;
import com.example.beginnerfitbe.attendance.repostitory.AttendanceRepository;
import com.example.beginnerfitbe.challenge.service.ChallengeService;
import com.example.beginnerfitbe.error.StateResponse;
import com.example.beginnerfitbe.jwt.util.JwtUtil;
import com.example.beginnerfitbe.redis.service.RedisService;
import com.example.beginnerfitbe.user.domain.User;
import com.example.beginnerfitbe.user.dto.SignInReqDto;
import com.example.beginnerfitbe.user.dto.SignInResDto;
import com.example.beginnerfitbe.user.dto.SignUpReqDto;
import com.example.beginnerfitbe.user.dto.UserDto;
import com.example.beginnerfitbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ChallengeService challengeService;
    private final RedisService redisService;

    public User signUp(SignUpReqDto dto) {
        String email = dto.getEmail();
        String name = dto.getName();
        String password = passwordEncoder.encode(dto.getPassword());

        // 중복 이메일 확인
        if (userService.emailCheck(email)) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        // 중복 닉네임 확인
        else if (userService.nameCheck(name)) {
            throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
        }

        // 회원 기본 정보만 입력
        User newUser = User.builder()
                .email(email)
                .name(name)
                .password(password)
                .profileUrl(null)
                .height(0)
                .weight(0)
                .targetWeight(0)
                .date(null)
                .targetDate(null)
                .exerciseTime(0)
                .exerciseIntensity(new ArrayList<>())
                .concernedAreas(new ArrayList<>())
                .exerciseGoals(new ArrayList<>())
                .build();

        userRepository.save(newUser); // 사용자 저장

        // 새로 가입한 사용자에게 랜덤 챌린지 부여
        challengeService.assignChallengesToUser(newUser);

        return newUser;
    }
    public SignInResDto signIn(SignInReqDto dto) {
        try {
            System.out.println("Starting sign in process for email: " + dto.getEmail());
            UserDto userDto = userService.readByEmail(dto.getEmail());

            if (userDto == null) {
                throw new IllegalArgumentException("User not found");
            }

            System.out.println("User found: " + userDto.getEmail());

            if (passwordEncoder.matches(dto.getPassword(), userDto.getPassword())) {
                System.out.println("Password match successful for user: " + userDto.getEmail());
                LocalDate today = LocalDate.now();

                boolean hasAttendance = attendanceRepository.existsByUserIdAndPresentDate(userDto.getId(), today);
                System.out.println("Attendance record exists: " + hasAttendance);

                // 출석 기록이 없으면 새로 생성
                if (!hasAttendance) {
                    Attendance attendance = new Attendance();
                    attendance.setPresentDate(today);
                    User user = new User();
                    user.setId(userDto.getId());
                    attendance.setUser(user);
                    attendanceRepository.save(attendance); // Attendance 저장
                    System.out.println("New attendance record created for user: " + userDto.getEmail());
                }

                return new SignInResDto(
                        userDto.getId(),
                        jwtUtil.generateAccessToken(userDto.getEmail(), userDto.getId()),
                        jwtUtil.generateRefreshToken(userDto.getEmail())
                );
            } else {
                throw new IllegalArgumentException("Invalid password");
            }
        } catch (Exception e) {
            // 에러 로그 출력
            System.err.println("Error during sign in: " + e.getMessage());
            e.printStackTrace();
            throw e; // 혹은 적절한 사용자 정의 예외로 래핑
        }
    }

    public StateResponse resetPassword(String email, String password){
        String newPassword=passwordEncoder.encode(password);
        return userService.resetPassword(email,newPassword);
    }

    public SignInResDto refresh(String refreshToken) {
        String email;

        try {
            email = redisService.getEmailByRefreshToken(refreshToken);

            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Invalid refresh token");
            }

            jwtUtil.validateRefreshToken(email, refreshToken);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Expired refresh token")) {
                throw new IllegalArgumentException("Refresh token has expired, please log in again");
            } else {
                throw new IllegalArgumentException("Invalid refresh token");
            }
        }

        UserDto userDto = userService.readByEmail(email);
        return new SignInResDto(
                userDto.getId(),
                jwtUtil.generateAccessToken(userDto.getEmail(), userDto.getId()),
                refreshToken
        );
    }


    public StateResponse signOut(Long userId) {
        UserDto userDto = userService.read(userId);
        if (userDto != null) {
            boolean tokenDeleted = jwtUtil.deleteRegisterToken(userDto.getEmail());
            if (tokenDeleted) {
                return StateResponse.builder()
                        .code("SUCCESSFUL")
                        .message("로그아웃 되었습니다.")
                        .build();
            } else {
                return StateResponse.builder()
                        .code("FAILED")
                        .message("로그아웃 실패. 토큰 삭제에 실패했습니다.")
                        .build();
            }
        } else {
            return StateResponse.builder()
                    .code("FAILED")
                    .message("사용자를 찾을 수 없습니다.")
                    .build();
        }
    }


}