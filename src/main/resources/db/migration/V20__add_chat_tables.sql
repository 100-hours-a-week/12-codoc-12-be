CREATE TABLE `chat_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `password` varchar(100) DEFAULT NULL,
  `participant_count` int NOT NULL,
  `last_message_id` bigint NOT NULL DEFAULT 0,
  `last_message_preview` varchar(50) NOT NULL,
  `last_message_at` timestamp(6) NOT NULL,
  `is_deleted` tinyint(1) NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` timestamp(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_room_is_deleted_last_message` (`is_deleted`,`last_message_at` DESC,`last_message_id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_room_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `chat_room_id` bigint NOT NULL,
  `joined_message_id` bigint NOT NULL DEFAULT 0,
  `last_read_message_id` bigint NOT NULL DEFAULT 0,
  `is_joined` tinyint(1) NOT NULL,
  `joined_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `leaved_at` timestamp(6) NULL DEFAULT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chat_room_participant_user_room` (`user_id`,`chat_room_id`),
  KEY `idx_chat_room_participant_user_joined_room` (`user_id`,`is_joined`,`chat_room_id`),
  CONSTRAINT `fk_chat_room_participant_chat_room` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint DEFAULT NULL,
  `chat_room_id` bigint NOT NULL,
  `type` varchar(20) NOT NULL,
  `content` varchar(500) NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_chat_room_id_id_desc` (`chat_room_id`,`id` DESC),
  CONSTRAINT `fk_chat_message_chat_room` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
