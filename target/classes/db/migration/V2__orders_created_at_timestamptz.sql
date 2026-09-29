-- The entity maps created_at to java.time.Instant, which Hibernate 6 expects
-- as TIMESTAMP WITH TIME ZONE. V1 created it as plain TIMESTAMP, which fails
-- ddl-auto: validate. Existing values were written by now() in the DB session
-- and are treated as UTC here.
ALTER TABLE orders ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE orders ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';
ALTER TABLE orders ALTER COLUMN created_at SET DEFAULT now();
