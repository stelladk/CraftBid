USE [craftbid]
GO
CREATE TRIGGER updatePriceTrigger on Offer
AFTER INSERT AS BEGIN
UPDATE Listing
SET Listing.min_price = INSERTED.price
FROM INSERTED
WHERE Listing.id = inserted.submitted_for
END
GO