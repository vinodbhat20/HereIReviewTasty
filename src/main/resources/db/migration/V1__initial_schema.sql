-- Initial schema for HereIReviewTasty
-- Mirrors the JPA entities in com.restaurant.domain.
-- IDs are generated in the application (Hibernate GenerationType.UUID),
-- so primary key columns carry no server-side default.

CREATE TABLE restaurants (
    id               UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    location         VARCHAR(255),
    cuisine_type     VARCHAR(100),
    average_rating   DOUBLE PRECISION,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP
);

CREATE TABLE reviews (
    id               UUID PRIMARY KEY,
    restaurant_id    UUID NOT NULL REFERENCES restaurants (id),
    reviewer_name    VARCHAR(255),
    review_text      TEXT NOT NULL,
    rating           INTEGER CHECK (rating BETWEEN 1 AND 5),
    created_at       TIMESTAMP NOT NULL
);

CREATE INDEX idx_reviews_restaurant_id ON reviews (restaurant_id);

CREATE TABLE dishes (
    id               UUID PRIMARY KEY,
    restaurant_id    UUID NOT NULL REFERENCES restaurants (id),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    price            NUMERIC(10, 2),
    created_at       TIMESTAMP NOT NULL
);

CREATE INDEX idx_dishes_restaurant_id ON dishes (restaurant_id);

CREATE TABLE quality_keywords (
    id               UUID PRIMARY KEY,
    aspect           VARCHAR(50) NOT NULL,
    sentiment        VARCHAR(20) NOT NULL CHECK (sentiment IN ('positive', 'negative')),
    keyword          VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP NOT NULL
);

CREATE INDEX idx_quality_keywords_aspect_sentiment ON quality_keywords (aspect, sentiment);

CREATE TABLE review_analyses (
    id                  UUID PRIMARY KEY,
    restaurant_id       UUID NOT NULL REFERENCES restaurants (id),
    analysis_json       JSONB,
    top_dishes          JSONB,
    quality_ratings     JSONB,
    concerns            TEXT[],
    praises             TEXT[],
    confidence          DOUBLE PRECISION,
    model_used          VARCHAR(100),
    processing_time_ms  INTEGER,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP
);

CREATE INDEX idx_review_analyses_restaurant_id ON review_analyses (restaurant_id);
