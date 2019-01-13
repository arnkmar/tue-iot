echo "ADMINISTRATOR VIEW - REGISTERED VEHICLES"
echo "           "
 sqlite3 IoTParking.db <<EOSs
.mode column
.headers on
.width 12 10 15 5 6 8
SELECT * FROM REGISTERED_VEHICLES;
EOSs
