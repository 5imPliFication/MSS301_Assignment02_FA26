-- Container only: SQL Server requires a complex bootstrap password on first start.
-- Match the assignment's sa/sa login after bootstrap, inside the isolated container.
:r /init-databases.sql
ALTER LOGIN sa WITH CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF;
ALTER LOGIN sa WITH PASSWORD = 'sa';
GO
