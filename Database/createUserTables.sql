CREATE TABLE UserInfo (
	username VARCHAR(20) PRIMARY KEY,
	password VARCHAR(30) NOT NULL,
	fullname VARCHAR(50) NOT NULL ,
	email VARCHAR(30) NOT NULL,
	phoneNumber VARCHAR(15),
	description VARCHAR(120),
	photo VARCHAR(100)
);

CREATE TABLE Creator (
	username VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username),
	isFreelancer BIT NOT NULL,
	phoneNumber VARCHAR(15) NOT NULL
);

CREATE TABLE Expertise (
	name VARCHAR(20) PRIMARY KEY
);
ALTER TABLE Creator 
ADD hasExpertise VARCHAR(20) FOREIGN KEY REFERENCES Expertise(name) NOT NULL;