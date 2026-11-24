-- Fase B: nick e avatar das contas, e nota em meias estrelas.

-- Nick único (o "@nick" do perfil), sempre em minúsculas. Contas antigas ganham "user<id>" e podem trocar na tela de
-- perfil; contas novas recebem um nick gerado a partir do nome (AuthService).
ALTER TABLE users ADD COLUMN handle VARCHAR(20);
ALTER TABLE users ADD COLUMN avatar_key VARCHAR(20) DEFAULT 'rosa' NOT NULL;
UPDATE users SET handle = 'user' || CAST(id AS VARCHAR(10));
ALTER TABLE users ALTER COLUMN handle SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_handle UNIQUE (handle);
ALTER TABLE users ADD CONSTRAINT ck_users_handle_minusculo CHECK (handle = LOWER(handle));

-- Nota guardada em meias estrelas, como inteiro de 1 a 10 (2,5 estrelas = 5): sem decimal no banco, então sem erro de
-- arredondamento. As notas que já existem (1 a 5) viram o dobro.
ALTER TABLE reviews ADD COLUMN rating_x2 INT;
UPDATE reviews SET rating_x2 = rating * 2;
ALTER TABLE reviews ALTER COLUMN rating_x2 SET NOT NULL;
ALTER TABLE reviews ADD CONSTRAINT ck_reviews_rating_x2 CHECK (rating_x2 BETWEEN 1 AND 10);
ALTER TABLE reviews DROP CONSTRAINT ck_reviews_rating;
ALTER TABLE reviews DROP COLUMN rating;
