CREATE TABLE Notification (
	listing_id INT FOREIGN KEY REFERENCES Listing(id) PRIMARY KEY,
	belongs_to VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	price FLOAT NOT NULL,
	photo VARCHAR(40) NOT NULL
);