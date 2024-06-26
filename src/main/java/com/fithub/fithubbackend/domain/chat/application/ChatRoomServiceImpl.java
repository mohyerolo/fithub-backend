package com.fithub.fithubbackend.domain.chat.application;

import com.fithub.fithubbackend.domain.chat.domain.Chat;
import com.fithub.fithubbackend.domain.chat.domain.ChatRoom;
import com.fithub.fithubbackend.domain.chat.dto.ChatRoomResponseDto;
import com.fithub.fithubbackend.domain.chat.repository.ChatRepository;
import com.fithub.fithubbackend.domain.chat.repository.ChatRoomRepository;
import com.fithub.fithubbackend.domain.user.domain.User;
import com.fithub.fithubbackend.domain.user.repository.UserRepository;
import com.fithub.fithubbackend.global.exception.CustomException;
import com.fithub.fithubbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public List<ChatRoomResponseDto> findChatRoomDesc(User user) {
        // Chat 테이블: 현재 유저의 채팅방 id와 채팅방 이름 가져옴
        List<Chat> chatList = this.chatRepository.findByChatPK_UserIdAndDeletedFalse(user.getId());

        List<ChatRoomResponseDto> dtoList = chatList.stream()
                .map(ChatRoomResponseDto::new)
                .collect(Collectors.toList());

        // modifiedDate 기준으로 정렬
        Comparator<ChatRoomResponseDto> comparator = Comparator.comparing(ChatRoomResponseDto::getLastMessageDate);
        Collections.sort(dtoList, comparator.reversed());

        return dtoList;
    }

    @Transactional
    @Override
    public Long save(User user, Long receiverId) {
        // chatRoom 테이블에 저장
        ChatRoom chatRoom = this.chatRoomRepository.save(new ChatRoom());

        // 상대 유저 찾기
        User receiverUser = userRepository.findById(receiverId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상대 유저가 존재하지 않음"));

        // 채팅룸ID-본인ID
        Chat chat = Chat.builder()
                .chatRoom(chatRoom)
                .chatRoomName(receiverUser.getNickname())
                .user(user)
                .build();
        this.chatRepository.save(chat);

        // 채팅룸ID-상대ID
        chat = Chat.builder()
                .chatRoom(chatRoom)
                .chatRoomName(user.getNickname())
                .user(receiverUser)
                .build();
        this.chatRepository.save(chat);

        return chatRoom.getRoomId();
    }

    @Transactional
    @Override
    public void deleteChatRoom(Long userId, Long roomId) {
        List<Chat> chatList = chatRepository.findByChatPK_ChatRoomRoomId(roomId);

        boolean needToAllDelete = chatList.stream().anyMatch(Chat::isDeleted);
        if (needToAllDelete) {
            deleteAllAssociatedChat(chatList);
        } else {
            deleteOnlyUsersChat(chatList, userId);
        }
    }

    private void deleteAllAssociatedChat(List<Chat> chatList) {
        chatRepository.deleteAll(chatList);
        chatRoomRepository.delete(chatList.get(0).getChatPK().getChatRoom());
    }

    private void deleteOnlyUsersChat(List<Chat> chatList, Long userId) {
        Chat userChat = chatList.stream()
                .filter(chat -> chat.getChatPK().getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자의 채팅이 존재하지 않음"));
        userChat.deleteChat();
    }

    @Override
    public Long hasChatRoom(Long userId, Long receiverId) {
       List<Long> roomIdsOfUser = chatRepository.findByChatPK_UserIdAndDeletedFalse(userId)
               .stream()
               .map(Chat::getChatRoomId)
               .collect(Collectors.toList());

        List<Long> roomIdsOfReceiver = chatRepository.findByChatPK_UserIdAndDeletedFalse(receiverId)
                .stream()
                .map(Chat::getChatRoomId)
                .collect(Collectors.toList());

        roomIdsOfUser.retainAll(roomIdsOfReceiver);

        return roomIdsOfUser.isEmpty() ? null : roomIdsOfUser.get(0);
    }

    @Override
    public Boolean isReceiverExists(Long userId, Long roomId) {
        Chat receiverChat = chatRepository.findByChatPK_ChatRoomRoomIdAndChatPK_UserIdNot(roomId, userId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND, "해당 채팅방의 다른 사용자를 찾을 수 없습니다."));
        return !receiverChat.isDeleted();
    }
}
