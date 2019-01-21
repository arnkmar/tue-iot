echo "                                ADMINISTRATOR VIEW for 'Parking-Spot-4'"
echo "           "
 sqlite3 IoTParking.db <<EOSs
.mode column
.headers on
.width 12 10 11 13 12 11
select RSTART AS ReservedFrom,Rend AS ReservedTo, RCar as ReservedFor, pcar as ParkedVehicle, Pstart as VehicleEntry, pend as VehicleExit, Valid as ValidityOfParking ,printf("%.2f", RATE) AS BillingRate from[Parking-Spot-4];

EOSs
