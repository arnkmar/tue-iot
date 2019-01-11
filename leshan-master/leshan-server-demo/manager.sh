echo "ADMINISTRATOR VIEW"
echo "           "
 sqlite3 IoTParking.db <<EOSs
.mode column
.headers on
     SELECT PIID,STATUS,STATE,CARNUMBER,PVALIDITY FROM OVERVIEW;
EOSs
