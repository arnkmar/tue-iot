const sqlite3 = require('sqlite3').verbose();
 
// open the database
let db = new sqlite3.Database('/home/arunkumar/eclipse-workspace/leshan-master/leshan-server-demo/IoTParking.db', sqlite3.OPEN_READWRITE, (err) => {
  if (err) {
    console.error(err.message);
  }
  console.log('Connected to the chinook database.');
});
 
db.serialize(function() {
	db.all("SELECT * FROM IoTParking;", function(err, rows) {
        console.log(err);
        console.log(rows);
   });
});
 
db.close((err) => {
  if (err) {
    console.error(err.message);
  }
  console.log('Close the database connection.');
});
