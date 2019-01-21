import requests
import json
import datetime
import time
import sys
import os

Serverip="192.168.178.44"


def register() :
	print("\n\n--------------------------------\n")
	print "Welcome to Registration Service\n"
	VLPN =raw_input('Enter your Vehicle Licence Plate Number\n  ->    ')
	UVID =raw_input('Enter a unique Vehicle Identification\n  ->    ')
	
	query = "http://"+Serverip+":8080/api/clients/vehicleRegister/"+str(VLPN)+"/"+str(UVID)
	r = requests.put(query)
	response=r.content
	if response=='Failed':
		print "Use a different 'Unique Vehicle Identification'. \n\n Try Again.\n"

	elif response=='Success':
		print "\n  <<< Registration of Vehicle Successful >>> \n"
		print "You can now Reserve Parking Spots."
	print("\n--------------------------------\n")
	raw_input('')
	os.system('clear')
	

def reserve() :
	print("\n\n--------------------------------\n")
	print "\nWelcome to Reservation Service\n"
		
	VehiclePlateNumber =raw_input('Enter Vehicle Licence Plate Number -> \n')
	
	dt1 =raw_input('Input Format to enter reservation time - > dd/mm/yyyy hh:mm:ss \nEnter Start Time: ')
	dt2 = raw_input('Enter Number of hours: ')
	StartTime = int(time.mktime(datetime.datetime.strptime(dt1,"%d/%m/%Y %H:%M:%S").timetuple()))
	End = int(time.mktime(datetime.datetime.strptime(dt2,"%d/%m/%Y %H:%M:%S").timetuple()))
	'''
	StartTime=int(time.time())+5
	End = StartTime+60
	'''
	#print "Reservation Start Time : "+datetime.datetime.fromtimestamp(StartTime).strftime('%Y-%m-%d %H:%M:%S')
	#print "Reservation End Time : "+datetime.datetime.fromtimestamp(End).strftime('%Y-%m-%d %H:%M:%S')
	#VehiclePlateNumber = "aa"

	query = "http://"+Serverip+":8080/api/clients/httpQuery/"+str(StartTime)+"/"+str(End)+"/"+str(VehiclePlateNumber)
	
	r = requests.put(query)

	if r.status_code==200:

		response=r.content.split(";")
		availableSpots=response[0].split(",")
		vehicleDetails = response[1].split(",")

		if response[1] == str('Please Register your vehicle first')  : 
			print '\n\nPlease Register your vehicle first'
		else:
			if vehicleDetails[1] == str('YES')  : # CRIMINAL RECORD
				print "\nSorry! Your vehicle been BlackListed due to a crimial record.\n You cannot avail the service."
				print "\nPlease contact us if you think we have made a mistake.\n"
				print ""
			elif vehicleDetails[2] != str('0.0')  : # parking fees Dues
				print "\nYou have unpaid dues for the earlier service(s). \n Please repay them to avail the service."
				print "\nPlease contact us if you think we have made a mistake.\n"
				print ""
			else :
				if len(availableSpots)<2  :
					print "\n\nNo spots are available for the requested time."
				else:
					print "\n\nBelow are the list of available spots for the requested time.\n "
					print "SpotID"+"   "+"SpotName\n"
					for i in range(len(availableSpots)):
						if availableSpots[i] != 'null':
							print str(i)+"        "+availableSpots[i]


					
					Choice =input('\nEnter a SpotID to reserve: ')
					if Choice > i:
						print "\nIncorrect SpotID. Try Again."
					else :
						query = "http://"+Serverip+":8080/api/clients/choice/"+str(StartTime)+"/"+str(End)+"/"+str(availableSpots[Choice])+"/"+str(VehiclePlateNumber)

						r = requests.put(query)
						if r.status_code==200:
							resp= r.content.split(",")
							print "\nReferenceID = ",resp[2]
							print "Reservation Start Time : "+datetime.datetime.fromtimestamp(StartTime).strftime('%Y-%m-%d %H:%M:%S')
							print "Reservation End Time : "+datetime.datetime.fromtimestamp(End).strftime('%Y-%m-%d %H:%M:%S')
						else :
							print "Request not completed. Try Again"

	else :
		print "Request not completed. Try Again"


	print("\n--------------------------------\n")
	print("\n--------------------------------\n")
	raw_input('')
	os.system('clear')

def main():
	
	print("\n**********************************************\n")	
	print("         Parking Reservation System             ")
	print("\n**********************************************\n")	
	print("Enter '1' to Register your Vehicle")
	print("Enter '2' to Reserve a ParkingSpot\n\n")
	choice =input('Enter your choice: ')
	#print "\nYou entered : ", dt1

	if choice==1:
		register()
	elif choice==2:
		reserve()
	else:
		print "Incorrect key pressed please retry"



if __name__ == '__main__':
	
	os.system('clear')
	while True:    
		main()
