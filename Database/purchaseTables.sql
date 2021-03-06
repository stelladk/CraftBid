CREATE TABLE Purchase (
	id INT IDENTITY(1,1) PRIMARY KEY,
	done_by VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	done_on INT NOT NULL,
	date DATE NOT NULL
);

CREATE TABLE Offer (
	id INT IDENTITY(1,1) PRIMARY KEY,
	price FLOAT NOT NULL,
	submitted_by VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	submitted_for INT FOREIGN KEY REFERENCES Listing(id) NOT NULL
);