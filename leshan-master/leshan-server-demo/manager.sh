echo "ADMINISTRATOR VIEW"
echo "           "
 sqlite3 IoTParking.db <<EOSs
.mode column
.headers on
     SELECT PIID,EVENT,STATE,CARNUMBER FROM OVERVIEW;
EOSs
