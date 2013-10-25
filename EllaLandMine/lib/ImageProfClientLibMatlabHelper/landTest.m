connection = ImageConnection();
connection.connect('127.0.0.1', 8887);
feature = connection.landQuery();
celldisp(feature);