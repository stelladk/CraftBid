CREATE TABLE Notification (
	id INT IDENTITY(1,1) PRIMARY KEY,
	listing_id INT FOREIGN KEY REFERENCES Listing(id),
	belongs_to VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	price FLOAT NOT NULL
);