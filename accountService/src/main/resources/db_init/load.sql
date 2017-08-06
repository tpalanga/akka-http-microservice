CREATE DATABASE IF NOT EXISTS account_srv CHARACTER SET utf8;

USE account_srv;

CREATE TABLE IF NOT EXISTS accounts(
  email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  PRIMARY KEY (email)
);

INSERT INTO accounts(email, username) VALUES ("test@test.com", "Test Account");

