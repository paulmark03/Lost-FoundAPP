package com.example.demoilost;
import com.example.demoilost.ChatUtils;
import org.junit.Test;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class ChatUtilsTest {



    @Test
    public void testIsValidMessage_withValidText() {
        assertTrue(com.example.demoilost.ChatUtils.isValidMessage("hi there"));
    }

    @Test
    public void testIsValidMessage_withEmptyText() {
        assertFalse(com.example.demoilost.ChatUtils.isValidMessage("  "));
        assertFalse(com.example.demoilost.ChatUtils.isValidMessage(""));
        assertFalse(com.example.demoilost.ChatUtils.isValidMessage(null));
    }
}
