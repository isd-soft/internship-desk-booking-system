CREATE SEQUENCE id_favourite_desk_seq START 1;

CREATE TABLE favourite_desks (
    id INTEGER DEFAULT nextval('id_favourite_desk_seq') PRIMARY KEY,
    user_id INTEGER NOT NULL,
    desk_id INTEGER NOT NULL,

    CONSTRAINT fk_favourite_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favourite_desk
        FOREIGN KEY (desk_id)
        REFERENCES desk (id)
        ON DELETE CASCADE,

    CONSTRAINT unique_user_desk UNIQUE (user_id, desk_id)
);