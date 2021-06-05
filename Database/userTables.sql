CREATE TABLE UserInfo (
	username VARCHAR(20) PRIMARY KEY,
	password VARCHAR(30) NOT NULL,
	fullname VARCHAR(50) NOT NULL ,
	email VARCHAR(30) NOT NULL UNIQUE,
	phoneNumber VARCHAR(15),
	description VARCHAR(120)
);

CREATE TABLE Expertise (
	name VARCHAR(20) PRIMARY KEY
);

CREATE TABLE Creator (
	username VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) PRIMARY KEY,
	isFreelancer BIT NOT NULL,
	phoneNumber VARCHAR(15) NOT NULL,
	hasExpertise VARCHAR(20) FOREIGN KEY REFERENCES Expertise(name) NOT NULL
);
