package com.example.beginnerfitbe.challenge.controller;

import com.example.beginnerfitbe.challenge.config.ChallengeScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    @Autowired
    private ChallengeScheduler challengeScheduler;

    //예시를 만들기 위한 TEST용 api
    @GetMapping("/test")
    public String testSchedule() {
        challengeScheduler.testSchedule(); // 수동 호출
        return "Scheduled task executed!";
    }
}
