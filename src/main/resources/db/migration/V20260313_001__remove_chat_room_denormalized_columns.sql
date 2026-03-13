ALTER TABLE `chat_room`
  DROP INDEX `idx_chat_room_is_deleted_last_message`,
  DROP COLUMN `participant_count`,
  DROP COLUMN `last_message_id`,
  DROP COLUMN `last_message_preview`,
  DROP COLUMN `last_message_at`;
