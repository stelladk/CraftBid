CREATE TABLE Evaluation (
	id INT IDENTITY(1,1) PRIMARY KEY,
	submitted_by VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	refers_to VARCHAR(20) FOREIGN KEY REFERENCES UserInfo(username) NOT NULL,
	rating INT NOT NULL,
	date DATE NOT NULL
);

-- github issue related
ALTER TABLE Evaluation ADD comment VARCHAR(100);