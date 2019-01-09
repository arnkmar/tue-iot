import requests
import json
import datetime
import time


dt1 =raw_input('Enter Start Time: ')
dt2 = raw_input('Enter Number of hours: ')


StartTime = int(time.mktime(datetime.datetime.strptime(dt1,"%d/%m/%Y %H:%M:%S").timetuple()))
End = int(time.mktime(datetime.datetime.strptime(dt2,"%d/%m/%Y %H:%M:%S").timetuple()))

query = "http://192.168.178.44:8080/api/clients/httpQuery/"+str(StartTime)+"/"+str(End)

r = requests.put(query)
availableSpots=r.content.split(",")

print "SpotID"+"       "+"SpotName"
if availableSpots[0] == '':
	print "No spots are availabel for the requested time"
else:
	for i in range(len(availableSpots)):
		if availableSpots[i] != 'null':
			print str(i)+"    "+availableSpots[i]


#toPi = requests.put("http://192.168.178.39:8080/api/clients/LeshanClientDemo/32700/0/32802/",json={'id':32802,'value':'test123'})
#toPi = requests.put("http://192.168.178.44:8080/api/clients/choice/123/321/LeshanClientDemo/car123",json={'id':32802,'value':'test123'})

#INSERT inTO REGISTERED_VEHICLES (VEHID,LICPLNUM) values ('V2','5678');


#response = resp[1:-1]
#js = json.loads(response)


Choice =input('Enter SpotID to reserve: ')
Carnumber =raw_input('Enter Car LicensePlate number: ')

query = "http://192.168.178.44:8080/api/clients/choice/"+str(StartTime)+"/"+str(End)+"/"+str(availableSpots[Choice])+"/"+str(Carnumber)

r = requests.put(query)
print "done"

r = requests.put(query)
resp= r.content
print resp


#CREATE TABLE RESERVATION (PIID TEXT PRIMARy KEY , H1 TEXT, H2 TEXT, H3 TEXT,H4 TEXT, H5 TEXT, H6 TEXT, H7 TEXT, H8 TEXT, H9 TEXT,H10 TEXT, H11 TEXT, H12 TEXT, H13 TEXT, H14 TEXT, H15 TEXT,H16 TEXT, H17 TEXT, H18 TEXT, H19 TEXT, H20 TEXT, H21 TEXT,H22 TEXT, H23 TEXT, H24 TEXT);

'''
CREATE TABLE rep_reuniao2 (ID INT, ROOM INT, DATE_BEGIN INT, DATE_END INT);


INSERT into LeshanClientDemo (TIME,RSTART,REND) values (100001,4,5);

SELECT rep_reuniao2.ID, CASE WHEN EXISTS ( 

SELECT * 
FROM rep_reuniao2 r
WHERE (12 BETWEEN r.DATE_BEGIN AND r.DATE_END)
    OR (13 BETWEEN r.DATE_BEGIN AND r.DATE_END)
	    OR (12 <= r.DATE_BEGIN AND 13 >= r.DATE_END)

	) THEN 'FALSE' ELSE 'TRUE' ;


SELECT  (case when count(*) = 0 then 'false' else 'true' end) as HasOverlappingRooms
FROM LeshanClientDemo r
WHERE (2 BETWEEN r.RSTART AND r.REND)
    OR (5 BETWEEN r.RSTART AND r.REND)
	    OR (2 <= r.RSTART AND 5 >= r.REND);'''
