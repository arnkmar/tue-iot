echo "ADMINISTRATOR VIEW"
echo "           "
 sqlite3 IoTParking.db <<EOSs
.mode column
.headers on
.width 20 10 9 10 8
SELECT PIID,STATUS,STATE,CARNUMBER,PVALIDITY FROM OVERVIEW;

EOSs
