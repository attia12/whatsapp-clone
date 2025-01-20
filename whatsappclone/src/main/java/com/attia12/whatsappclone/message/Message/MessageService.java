package com.attia12.whatsappclone.message.Message;

import com.attia12.whatsappclone.chat.Chat;
import com.attia12.whatsappclone.chat.ChatRepository;
import com.attia12.whatsappclone.file.FileService;
import com.attia12.whatsappclone.file.FileUtils;
import com.attia12.whatsappclone.notification.Notification;
import com.attia12.whatsappclone.notification.NotificationService;
import com.attia12.whatsappclone.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor

public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final FileService fileService;
    private final NotificationService notificationService;
    public void saveMessage(MessageRequest messageRequest) {
        Chat chat=chatRepository.findById(messageRequest.getChatId()).orElseThrow(()-> new EntityNotFoundException("chat not found"));
        Message message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);
        messageRepository.save(message);
        // todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(messageRequest.getType())
                .content(messageRequest.getContent())
                .senderId(messageRequest.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getTargetChatName(message.getSenderId()))
                .build();

        notificationService.sendNotification(messageRequest.getReceiverId(), notification);
    }
    public List<MessageResponse> findChatMessages(String chatId) {
        return messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();



    }
    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication) {
        Chat chat=chatRepository.findById(chatId).orElseThrow(()-> new EntityNotFoundException("chat not found"));
        final String recipientId=getReceipientId(chat,authentication);
        messageRepository.setMessagesToSeenByChatId(chatId,MessageState.SEEN);
        // todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .type(NotificationType.SEEN)
                .receiverId(recipientId)
                .senderId(getSenderId(chat, authentication))
                .build();

        notificationService.sendNotification(recipientId, notification);
    }
    public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        final String senderId = getSenderId(chat, authentication);
        final String receiverId = getReceipientId(chat, authentication);
        String filePath=fileService.saveFile(file,senderId);
        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);
        messageRepository.save(message);
        // todo notification
        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .type(NotificationType.IMAGE)
                .senderId(senderId)
                .receiverId(receiverId)
                .messageType(MessageType.IMAGE)
                .media(FileUtils.readFileFromLocation(filePath))
                .build();

        notificationService.sendNotification(receiverId, notification);
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if(chat.getSender().getId().equals(authentication.getName())){return chat.getSender().getId();}
        return chat.getRecipient().getId();
    }


    private String getReceipientId(Chat chat, Authentication authentication) {
        if(chat.getSender().getId().equals(authentication.getName())){return chat.getRecipient().getId();}
        return chat.getSender().getId();
    }


}
