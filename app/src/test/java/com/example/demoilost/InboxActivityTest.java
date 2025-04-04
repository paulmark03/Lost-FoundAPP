package com.example.demoilost;

import static org.junit.Assert.*;

import com.example.demoilost.model.ChatPreviewModel;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InboxActivityTest {

    private List<ChatPreviewModel> mockList;
    private ChatPreviewModel chat1;
    private ChatPreviewModel chat2;

    @Before
    public void setup() {
        mockList = new ArrayList<>();
        chat1 = new ChatPreviewModel();
        chat2 = new ChatPreviewModel();
        mockList.add(chat1);
        mockList.add(chat2);
    }




    @Test
    public void testContainsChat_returnsFalse_whenChatDoesNotExist() {
        assertFalse(containsChat("nonexistent"));
    }

    private boolean containsChat(String chatId) {
        for (ChatPreviewModel chat : mockList) {
            if (chatId != null && chatId.equals(chat.getChatId())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testDeleteChat_removesChatFromList() {
        int initialSize = mockList.size();
        mockList.remove(chat1);
        assertEquals(initialSize - 1, mockList.size());
        assertFalse(containsChat("chat123"));
    }
}
