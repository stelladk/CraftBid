CREATE TABLE Location (
	id INT IDENTITY(1,1) PRIMARY KEY,
	name VARCHAR(MAX) NOT NULL,
	longitude DECIMAL(10,7),
	latitude DECIMAL(10,7),
	UNIQUE(longitude, latitude)
);

CREATE TABLE Category (
	name VARCHAR(20) PRIMARY KEY
);

CREATE TABLE Listing (
	id INT IDENTITY(1,1) PRIMARY KEY,
	name VARCHAR(30) NOT NULL,
	description VARCHAR(130) NOT NULL,
	category VARCHAR(20) FOREIGN KEY REFERENCES Category(name) NOT NULL,
	min_price FLOAT DEFAULT(0),
	reward_points INT DEFAULT(0),
	quantity INT DEFAULT(1),
	is_located INT FOREIGN KEY REFERENCES Location(id) NOT NULL,
	published_by VARCHAR(20) FOREIGN KEY REFERENCES Creator(username)
);

CREATE TABLE Photo (
	id INT PRIMARY KEY FOREIGN KEY REFERENCES Listing(id),
	path VARCHAR(40) NOT NULL
);