-- ============================================================
-- Financial & Income Tax Assistance Platform — Database Schema
-- Engine: MySQL 8.x (InnoDB, utf8mb4)
-- Note: Fully portable to PostgreSQL with minor type changes
--   (AUTO_INCREMENT -> SERIAL, ENUM -> CHECK constraint, etc.)
-- ============================================================

CREATE DATABASE IF NOT EXISTS fintax_platform
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fintax_platform;

-- ------------------------------------------------------------
-- 1. ROLES & USERS
-- ------------------------------------------------------------
CREATE TABLE roles (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(50) NOT NULL UNIQUE      -- e.g. USER, CA, ADMIN
);

CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(150) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_id       BIGINT NOT NULL,
  learning_level ENUM('BEGINNER','INTERMEDIATE','ADVANCED') DEFAULT 'BEGINNER',
  is_active     BOOLEAN DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE admin_users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT NOT NULL UNIQUE,
  permissions   VARCHAR(255) DEFAULT 'FULL',     -- simple scope flag; expand later
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. CATEGORIES & ARTICLES (Financial Learning + Income Tax content)
-- ------------------------------------------------------------
CREATE TABLE categories (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(120) NOT NULL,
  slug          VARCHAR(140) NOT NULL UNIQUE,
  description   TEXT,
  parent_id     BIGINT NULL,                     -- self-reference for subcategories
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE articles (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  title               VARCHAR(200) NOT NULL,
  slug                VARCHAR(220) NOT NULL UNIQUE,
  category_id         BIGINT NOT NULL,
  beginner_content     LONGTEXT,
  intermediate_content LONGTEXT,
  advanced_content     LONGTEXT,
  examples            LONGTEXT,
  view_count          BIGINT DEFAULT 0,
  created_by          BIGINT NULL,                -- admin_users.id
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
  CONSTRAINT fk_article_admin FOREIGN KEY (created_by) REFERENCES admin_users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_articles_category ON articles(category_id);
CREATE FULLTEXT INDEX idx_articles_search ON articles(title, beginner_content, intermediate_content, advanced_content);

CREATE TABLE faqs (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id    BIGINT NULL,
  category_id   BIGINT NULL,
  question      VARCHAR(300) NOT NULL,
  answer        TEXT NOT NULL,
  level         ENUM('BEGINNER','INTERMEDIATE','ADVANCED') DEFAULT 'BEGINNER',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_faq_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  CONSTRAINT fk_faq_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. CA DIRECTORY
-- ------------------------------------------------------------
CREATE TABLE ca_profiles (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT NULL UNIQUE,             -- linked account, optional (demo data won't have one)
  full_name       VARCHAR(150) NOT NULL,
  qualification   VARCHAR(150),                   -- e.g. "CA, B.Com"
  city            VARCHAR(100) NOT NULL,
  state           VARCHAR(100) NOT NULL,
  pincode         VARCHAR(10),
  experience_years INT DEFAULT 0,
  languages       VARCHAR(255),                   -- comma-separated for simplicity
  bio             TEXT,
  contact_email   VARCHAR(150),
  contact_phone   VARCHAR(20),
  is_verified     BOOLEAN DEFAULT FALSE,
  rating_avg      DECIMAL(3,2) DEFAULT 0.00,
  rating_count    INT DEFAULT 0,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ca_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_ca_location ON ca_profiles(city, state, pincode);

CREATE TABLE ca_specializations (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL UNIQUE      -- Income Tax, GST, Audit, etc.
) ENGINE=InnoDB;

CREATE TABLE ca_profile_specializations (         -- many-to-many join table
  ca_profile_id       BIGINT NOT NULL,
  specialization_id   BIGINT NOT NULL,
  PRIMARY KEY (ca_profile_id, specialization_id),
  CONSTRAINT fk_cps_profile FOREIGN KEY (ca_profile_id) REFERENCES ca_profiles(id) ON DELETE CASCADE,
  CONSTRAINT fk_cps_spec FOREIGN KEY (specialization_id) REFERENCES ca_specializations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE reviews (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  ca_profile_id BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  rating        TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment       TEXT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_ca FOREIGN KEY (ca_profile_id) REFERENCES ca_profiles(id) ON DELETE CASCADE,
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_review_user_ca (user_id, ca_profile_id)  -- one review per user per CA
) ENGINE=InnoDB;

CREATE TABLE contact_requests (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT NULL,
  ca_profile_id BIGINT NOT NULL,
  name          VARCHAR(150) NOT NULL,
  email         VARCHAR(150) NOT NULL,
  phone         VARCHAR(20),
  message       TEXT,
  status        ENUM('PENDING','CONTACTED','CLOSED') DEFAULT 'PENDING',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_contact_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_contact_ca FOREIGN KEY (ca_profile_id) REFERENCES ca_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. USER ACTIVITY (saved content, history, calculators, notifications)
-- ------------------------------------------------------------
CREATE TABLE saved_articles (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT NOT NULL,
  article_id    BIGINT NOT NULL,
  saved_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
  UNIQUE KEY uq_saved_user_article (user_id, article_id)
) ENGINE=InnoDB;

CREATE TABLE search_history (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT NULL,                      -- nullable: guests can search too
  query         VARCHAR(300) NOT NULL,
  filters_json  JSON,
  searched_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_search_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_search_user ON search_history(user_id);

CREATE TABLE calculator_history (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT NOT NULL,
  calculator_type ENUM('INCOME_TAX','SIMPLE_INTEREST','COMPOUND_INTEREST','EMI','SAVINGS','INVESTMENT_RETURNS') NOT NULL,
  input_json      JSON NOT NULL,
  result_json     JSON NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_calc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id       BIGINT NOT NULL,
  title         VARCHAR(200) NOT NULL,
  message       TEXT NOT NULL,
  is_read       BOOLEAN DEFAULT FALSE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);

-- ------------------------------------------------------------
-- 5. SEED DATA (roles + a few categories/specializations to start)
-- ------------------------------------------------------------
INSERT INTO roles (name) VALUES ('USER'), ('CA'), ('ADMIN');

INSERT INTO categories (name, slug, description) VALUES
  ('Personal Finance', 'personal-finance', 'Budgeting, saving, and money management basics'),
  ('Income Tax', 'income-tax', 'Understanding income tax rules, slabs, and filing'),
  ('ITR Filing', 'itr-filing', 'Step-by-step guides for filing income tax returns'),
  ('Investments', 'investments', 'Investment options and strategies'),
  ('Savings', 'savings', 'Savings instruments and strategies'),
  ('Loans', 'loans', 'Loan types, EMI, and interest'),
  ('Insurance', 'insurance', 'Life, health, and other insurance basics'),
  ('Budgeting', 'budgeting', 'Planning and tracking personal budgets'),
  ('Tax Deductions', 'tax-deductions', 'Deductions and exemptions available under tax law'),
  ('GST', 'gst', 'Goods and Services Tax basics'),
  ('Business Finance', 'business-finance', 'Finance topics for freelancers and small businesses'),
  ('Retirement Planning', 'retirement-planning', 'Planning for retirement'),
  ('Financial Planning', 'financial-planning', 'Long-term financial goal planning');

INSERT INTO ca_specializations (name) VALUES
  ('Income Tax'), ('GST'), ('Audit'), ('Accounting'),
  ('Corporate Tax'), ('ITR Filing'), ('Business Consulting'), ('Financial Planning');

-- ------------------------------------------------------------
-- 6. SAMPLE / DEMO DATA (CA profiles) — for development/testing only
-- ------------------------------------------------------------
INSERT INTO ca_profiles (full_name, qualification, city, state, pincode, experience_years, languages, bio, contact_email, contact_phone, is_verified, rating_avg, rating_count) VALUES
  ('Demo CA Ananya Rao', 'CA, B.Com', 'Bengaluru', 'Karnataka', '560001', 8, 'English, Kannada, Hindi', 'Specializes in ITR filing and tax planning for salaried professionals.', 'demo.ananya@example.com', '9000000001', TRUE, 4.50, 12),
  ('Demo CA Rohit Sharma', 'CA, CS', 'Mumbai', 'Maharashtra', '400001', 12, 'English, Hindi, Marathi', 'Corporate tax and audit specialist for small businesses.', 'demo.rohit@example.com', '9000000002', TRUE, 4.70, 20),
  ('Demo CA Priya Menon', 'CA', 'Bengaluru', 'Karnataka', '560034', 5, 'English, Malayalam, Kannada', 'Focuses on GST compliance for freelancers and startups.', 'demo.priya@example.com', '9000000003', FALSE, 4.20, 6);

INSERT INTO ca_profile_specializations (ca_profile_id, specialization_id)
SELECT cp.id, cs.id FROM ca_profiles cp, ca_specializations cs
WHERE cp.full_name = 'Demo CA Ananya Rao' AND cs.name IN ('Income Tax', 'ITR Filing');

INSERT INTO ca_profile_specializations (ca_profile_id, specialization_id)
SELECT cp.id, cs.id FROM ca_profiles cp, ca_specializations cs
WHERE cp.full_name = 'Demo CA Rohit Sharma' AND cs.name IN ('Corporate Tax', 'Audit');

INSERT INTO ca_profile_specializations (ca_profile_id, specialization_id)
SELECT cp.id, cs.id FROM ca_profiles cp, ca_specializations cs
WHERE cp.full_name = 'Demo CA Priya Menon' AND cs.name IN ('GST', 'Business Consulting');

-- ------------------------------------------------------------
-- 7. SAMPLE / DEMO DATA (Articles + FAQs) — for development/testing only
-- ------------------------------------------------------------
INSERT INTO articles (title, slug, category_id, beginner_content, intermediate_content, advanced_content, examples)
SELECT 'What is TDS?', 'what-is-tds', c.id,
  'TDS means Tax Deducted at Source. It is tax that gets deducted from your income before you receive it, like from your salary.',
  'TDS (Tax Deducted at Source) is a mechanism where the payer deducts tax at prescribed rates before making a payment (salary, interest, rent, etc.) and deposits it with the government on the payee''s behalf.',
  'TDS is governed by Sections 192-206 of the Income Tax Act, with rates varying by payment type and payee status (resident/non-resident). Excess TDS can be claimed as a refund while filing ITR, and mismatches can be reconciled via Form 26AS/AIS.',
  'Example: If your salary is ₹50,000/month, your employer may deduct TDS based on your estimated annual tax liability before crediting your salary.'
FROM categories c WHERE c.slug = 'income-tax';

INSERT INTO articles (title, slug, category_id, beginner_content, intermediate_content, advanced_content, examples)
SELECT 'How to File ITR', 'how-to-file-itr', c.id,
  'ITR filing means reporting your income to the government every year so they know how much tax you owe or should refund.',
  'Filing an ITR (Income Tax Return) involves choosing the correct ITR form based on your income sources, reporting income, claiming deductions, and reconciling TDS with Form 26AS before submission on the income tax e-filing portal.',
  'ITR filing requires selecting the correct regime (old vs new), computing income under the right heads (salary, house property, capital gains, business/profession, other sources), applying eligible deductions/exemptions, and e-verifying the return within the prescribed time limit to avoid it being treated as invalid.',
  'Example: A salaried individual with only salary income and interest from savings typically files ITR-1 (Sahaj).'
FROM categories c WHERE c.slug = 'itr-filing';

INSERT INTO faqs (article_id, question, answer, level)
SELECT a.id, 'Can I get a refund if excess TDS was deducted?',
  'Yes — if the TDS deducted is more than your actual tax liability, you can claim the excess as a refund when you file your ITR.',
  'BEGINNER'
FROM articles a WHERE a.slug = 'what-is-tds';

INSERT INTO faqs (article_id, question, answer, level)
SELECT a.id, 'What happens if I file my ITR after the due date?',
  'You can still file a belated return before the extended deadline set each year, but it may attract late fees under Section 234F and you may lose the ability to carry forward certain losses.',
  'INTERMEDIATE'
FROM articles a WHERE a.slug = 'how-to-file-itr';

-- Note: sample notifications, saved_articles, reviews, contact_requests, and calculator_history
-- rows are intentionally NOT seeded here since they reference specific user_id values that only
-- exist after you register a user through /api/auth/register. Create a user first, then use the
-- corresponding API endpoints (save an article, submit a review, run a calculator) to populate them.
