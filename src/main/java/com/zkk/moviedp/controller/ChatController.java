package com.zkk.moviedp.controller;

import com.zkk.moviedp.assistant.MovieAgent;
import com.zkk.moviedp.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private MovieAgent movieAgent;

    @GetMapping("")
    public Result chat(@RequestParam(value = "memoryId") Integer memoryId,
                       @RequestParam(value = "message") String message) {
        return Result.ok(movieAgent.chat(memoryId, message));
    }

}
