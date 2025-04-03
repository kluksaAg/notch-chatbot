CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

CREATE TABLE employees
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    salary        DECIMAL(10, 2)
);
