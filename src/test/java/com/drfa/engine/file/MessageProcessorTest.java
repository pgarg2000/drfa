package com.drfa.engine.file;


import com.drfa.engine.file.MessageProcessor;
import org.junit.Test;

public class MessageProcessorTest {

    @Test
    public void testProcessMessageForDeterminingTheBase() throws Exception{
        String message = "BASE:T1|T2|T3|T4$T1|T2|T3|T4";
        MessageProcessor messageProcessor = new MessageProcessor(null);
    }
}
