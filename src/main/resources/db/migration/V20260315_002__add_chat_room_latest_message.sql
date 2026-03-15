CREATE TABLE `chat_room_latest_message` (
  `chat_room_id` bigint NOT NULL,
  `latest_text_message_id` bigint DEFAULT NULL,
  `latest_message_preview` varchar(50) NOT NULL,
  `latest_message_at` timestamp(6) NOT NULL,
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`chat_room_id`),
  KEY `idx_chat_room_latest_message_order` (`latest_message_at` DESC, `chat_room_id` DESC),
  CONSTRAINT `fk_chat_room_latest_message_chat_room`
    FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `chat_room_latest_message` (
  `chat_room_id`,
  `latest_text_message_id`,
  `latest_message_preview`,
  `latest_message_at`
)
SELECT
  r.id,
  lt.latest_text_message_id,
  LEFT(COALESCE(tm.content, im.content, ''), 50),
  COALESCE(tm.created_at, im.created_at, r.created_at)
FROM `chat_room` r
LEFT JOIN (
  SELECT
    m.chat_room_id,
    MAX(m.id) AS latest_text_message_id
  FROM `chat_message` m
  WHERE m.type = 'TEXT'
  GROUP BY m.chat_room_id
) lt ON lt.chat_room_id = r.id
LEFT JOIN `chat_message` tm
  ON tm.id = lt.latest_text_message_id
LEFT JOIN (
  SELECT
    m.chat_room_id,
    MAX(m.id) AS latest_init_message_id
  FROM `chat_message` m
  WHERE m.type = 'INIT'
  GROUP BY m.chat_room_id
) li ON li.chat_room_id = r.id
LEFT JOIN `chat_message` im
  ON im.id = li.latest_init_message_id;
