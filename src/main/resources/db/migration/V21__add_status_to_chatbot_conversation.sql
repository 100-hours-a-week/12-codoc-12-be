ALTER TABLE `chatbot_conversation`
  ADD COLUMN `status` varchar(20) NULL AFTER `is_correct`;

UPDATE `chatbot_conversation`
SET `status` = CASE
    WHEN `ai_message` IS NULL THEN 'DISCONNECTED'
    ELSE 'COMPLETED'
END
WHERE `status` IS NULL;

ALTER TABLE `chatbot_conversation`
  MODIFY COLUMN `status` varchar(20) NOT NULL;
