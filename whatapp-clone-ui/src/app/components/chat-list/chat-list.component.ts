import {Component, EventEmitter, input, InputSignal, Output} from '@angular/core';
import {ChatResponse} from "../../services/models/chat-response";
import {DatePipe} from "@angular/common";
import {UserResponse} from "../../services/models/user-response";
import {UserService} from "../../services/services/user.service";
import {ChatService} from "../../services/services/chat.service";
import {KeycloakService} from "../../utils/keycloak/keycloak.service";

@Component({
  selector: 'app-chat-list',
  imports: [
    DatePipe
  ],
  templateUrl: './chat-list.component.html',
  standalone: true,
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent {
  chats: InputSignal<ChatResponse[]>=input<ChatResponse[]>([]);
  searchNewContact=false;
  contacts:Array<UserResponse>=[];

  @Output() chatSelected = new EventEmitter<ChatResponse>();

  constructor(private userService: UserService,private chatService: ChatService,private keycloakService: KeycloakService) {
  }

  searchContact() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.contacts = users;
        this.searchNewContact = true;

      }
    })

  }

  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);

  }

  wrapMessage(lastMessage: string | undefined):string {
    if(lastMessage && lastMessage.length <= 20) {
      return lastMessage;

    }
    return lastMessage?.substring(0,17)+ '...';
  }

  selectContact(contact: UserResponse) {
    this.chatService.createChat({
      'sender-id': this.keycloakService.userId as string,
      'receiver-id': contact.id as string

    }).subscribe({
      next: (data) => {
        const chat:ChatResponse={
          id:data.response,
          name:contact.firstName + ' ' +contact.lastName,
          recipientOnline:contact.online,
          lastMessageTime:contact.lastSeen,
          senderId: this.keycloakService.userId,
          receiverId: contact.id,
        };
        this.chats().unshift(chat);
        this.searchNewContact=false;
        this.chatSelected.emit(chat);
      }
    })

  }
}
