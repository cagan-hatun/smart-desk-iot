package com.smartdesk.controller;

import com.smartdesk.model.UserProfile;
import com.smartdesk.service.ProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    private final ProfileService profileService;

    public DashboardController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/api/profile")
    @ResponseBody
    public UserProfile profile() {
        return profileService.getOrCreateProfile();
    }
}
