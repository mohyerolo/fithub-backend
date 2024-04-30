package com.fithub.fithubbackend.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fithub.fithubbackend.domain.chat.domain.ChatMessage;
import com.fithub.fithubbackend.global.domain.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ChatMessageResponseDto {

    @Schema(description = "메세지 id")
    private Long messageId;

//    @Schema(description = "발신자 id")
//    private Long senderId;

    @Schema(description = "발신자 구분")
    private boolean isMe;

    @Schema(description = "발신자 이메일")
    private String senderEmail;

    @Schema(description = "발신자 닉네임")
    private String senderNickname;

    @Schema(description = "발신자 프로필이미지")
    private Document senderProfileImg;

    @Schema(description = "메세지 내용")
    private String message;

    @Schema(description = "메세지 생성일")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;


    public ChatMessageResponseDto(ChatMessage entity, long userId) {
        this.messageId = entity.getMessageId();
        this.isMe = entity.getSender().getId() == userId? true : false;
        this.senderNickname = entity.getSender().getNickname();
        this.senderProfileImg = entity.getSender().getProfileImg();
        this.message = entity.getMessage();
        this.createdDate = entity.getCreatedDate();
    }
}
