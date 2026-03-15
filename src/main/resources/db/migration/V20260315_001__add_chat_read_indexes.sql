CREATE INDEX `idx_chat_room_participant_room_joined_user`
    ON `chat_room_participant` (`chat_room_id`, `is_joined`, `user_id`);

CREATE INDEX `idx_chat_message_chat_room_type_id_desc`
    ON `chat_message` (`chat_room_id`, `type`, `id` DESC);
