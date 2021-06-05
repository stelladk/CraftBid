-- after offer insertion update price of listing
GO
CREATE TRIGGER updatePriceTrigger on Offer
AFTER INSERT AS BEGIN
UPDATE Listing
SET Listing.min_price = INSERTED.price
FROM INSERTED
WHERE Listing.id = inserted.submitted_for
END
GO

-- after offer deletion update price of listing with MAX price offered
GO
CREATE TRIGGER updatePriceDeletionTrigger on Offer
AFTER DELETE AS BEGIN
UPDATE Listing
SET Listing.min_price = MAXPRICE
FROM Listing
JOIN (
	SELECT submitted_for, MAX(price) as MAXPRICE
	FROM Offer
	GROUP BY submitted_for
) AS UPDATEDOFFERS
ON Listing.id = UPDATEDOFFERS.submitted_for
END
GO