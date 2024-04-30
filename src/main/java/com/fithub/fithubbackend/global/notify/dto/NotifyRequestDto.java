package com.fithub.fithubbackend.global.notify.dto;

import com.fithub.fithubbackend.domain.user.domain.User;
import com.fithub.fithubbackend.global.notify.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotifyRequestDto {

    @NotNull
    private User receiver;

    @NotNull
    private String content;

    private String url;

    @NotNull
    private NotificationType type;

    @Builder
    public NotifyRequestDto(User receiver, Long urlId, String content, NotificationType type) {
        String url = switch (type) {
            case COMMENT, LIKE_POST -> "/post/" + urlId;
            case RESERVATION -> "/trainer/home";
            case REVIEW -> "/detail/" + urlId;
            case NOSHOW -> "/user/cancellations";
            default -> null;
        };

        this.receiver = receiver;
        this.content = content;
        this.url = url;
        this.type = type;
    }
}