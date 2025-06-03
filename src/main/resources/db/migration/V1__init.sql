CREATE SCHEMA IF NOT EXISTS tt;

CREATE TABLE tt.token_data
(
    id    BIGSERIAL PRIMARY KEY,
    token CHARACTER VARYING NOT NULL,
    data  CHARACTER VARYING NOT NULL,
    CONSTRAINT token_data_ukey UNIQUE (token)
);

CREATE TABLE tt.template_data
(
    id                 BIGSERIAL PRIMARY KEY,
    template_name      CHARACTER VARYING NOT NULL,
    template_condition CHARACTER VARYING NOT NULL,
    CONSTRAINT template_data_ukey UNIQUE (template_name)
);
