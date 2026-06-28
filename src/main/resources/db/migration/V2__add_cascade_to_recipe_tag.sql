ALTER TABLE `recipe_tag` DROP FOREIGN KEY `FKnk9vj1t1tc0rsdmlhm4rger7e`;
ALTER TABLE `recipe_tag` ADD CONSTRAINT `FKnk9vj1t1tc0rsdmlhm4rger7e` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`tag`) ON DELETE CASCADE ON UPDATE CASCADE;
