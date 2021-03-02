INSERT INTO person(id, name, deleted) VALUES (nextval('hibernate_sequence'), 'c', false );
INSERT INTO dog(id, name, race, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'a', 'b', false, 1);
INSERT INTO person(id, name, deleted) VALUES (nextval('hibernate_sequence'), 'd', false );
INSERT INTO dog(id, name, race, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'e', 'f', false, 3);
INSERT INTO dog(id, name, race, deleted, owner_id) VALUES (nextval('hibernate_sequence'), 'g', 'h', false, 1);