ALTER TABLE `chatbot_conversation`
  ADD COLUMN `message_type` varchar(20) NOT NULL
    AFTER `paragraph_type`;
