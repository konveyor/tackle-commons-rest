INSERT INTO breed(id, name, origin, deleted) VALUES (nextval('hibernate_sequence'), 'i', 'j', false);
INSERT INTO person(id, name, deleted) VALUES (nextval('hibernate_sequence'), 'c', false );
INSERT INTO dog(id, name, color, breed_id, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'a', 'b', 1, false, 2);
INSERT INTO person(id, name, deleted) VALUES (nextval('hibernate_sequence'), 'd', false );
INSERT INTO dog(id, name, color, breed_id, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'e', 'f', 1, false, 4);
INSERT INTO dog(id, name, color, breed_id, deleted) VALUES (nextval('hibernate_sequence'), 'K', 'l', 1, false);
INSERT INTO dog(id, name, color, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'g', 'h', false, 2);
