CREATE TABLE `recipe` (
  `recipe_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `published` bit(1) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`recipe_id`),
  FULLTEXT KEY `ft_recipe_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `ingredient` (
  `quantity` double DEFAULT NULL,
  `ingredient_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `recipe_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ingredient_id`),
  KEY `FKj0s4ywmqqqw4h5iommigh5yja` (`recipe_id`),
  CONSTRAINT `FKj0s4ywmqqqw4h5iommigh5yja` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;


CREATE TABLE `recipe_user` (
  `recipe_user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) NOT NULL,
  `recipe_id` bigint(20) NOT NULL,
  PRIMARY KEY (`recipe_user_id`),
  UNIQUE KEY `UKaa3xxrk3ergi3d3ny9o07sctk` (`user_id`,`recipe_id`),
  KEY `IDXbo4whi467hgc1s5vmo09vcnou` (`user_id`),
  KEY `IDXm61qtgnk37cc2ao17dkye8tkq` (`recipe_id`),
  CONSTRAINT `FK913bjnhapfinvoyrbkau9m32p` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `step` (
  `step_number` int(11) DEFAULT NULL,
  `recipe_id` bigint(20) DEFAULT NULL,
  `step_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instruction` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`step_id`),
  KEY `FKpwpbn24pd57073jm669d7dwt9` (`recipe_id`),
  CONSTRAINT `FKpwpbn24pd57073jm669d7dwt9` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `tag` (
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `recipe_tag` (
  `recipe_id` bigint(20) NOT NULL,
  `tag_id` varchar(255) NOT NULL,
  PRIMARY KEY (`recipe_id`,`tag_id`),
  KEY `FKnk9vj1t1tc0rsdmlhm4rger7e` (`tag_id`),
  CONSTRAINT `FKnk9vj1t1tc0rsdmlhm4rger7e` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag`),
  CONSTRAINT `FKshildcupwo2vlv8sjyxjlpi8l` FOREIGN KEY (`recipe_id`) REFERENCES `recipe` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;