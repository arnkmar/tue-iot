import os
import json
import time
import logging
import sys


logging.basicConfig(filename='parkingSpot_camera.log',level=logging.DEBUG)

def compareImg() :
	print "A change in image found"
	return True

def check():

	
	result = os.system("alpr check.jpg -c eu -n 1 -j > alprOut.json")
	exists = os.path.isfile('alprOut.json')


	if exists:
        	try:
			# Store configuration file values
		        jdata=open("alprOut.json").read()
	        	j= json.loads(jdata)
		        plate = j['results'][0]['plate']
		        confidence = j['results'][0]['confidence']
			logging.info('Number Plate Identified:'+plate)
			print "Number Plate Identified"
			return plate,confidence
		except IndexError:
			logging.info('No Number Plate Identified')
			print "No Plate found"
			return "0","0"

		except KeyboardInterrupt:
	         	print 'Interrupted'
        	 	sys.exit(0)

	else:
	        # Keep presets
		print "No JSON file generated"
	        os.system("rm -r check.jpg")
		return "0","0"


def main():

	lastPlate = "0"
	while True:
		try:
			os.system( "raspistill -o check.jpg")
			result = os.path.isfile('check.jpg')
			if result != 0:
				change = compareImg()
				if change :
					plate,confidence = check()
					print plate
					print lastPlate
					if plate!=lastPlate:
						if plate != '0':
							count = 3
							while count:
								os.system( "raspistill -o check.jpg")
								lastPlate = plate
								plate,confidence = check() # Perform ALPR . check for Plate number and confidence
								if (confidence>80) & (plate!=lastPlate)&(plate!='0') :
									count=count-1
								else :
									break
							if count==0:
								command ="cp check.jpg parkedVehicleImages/"+str(int(time.time()))+".jpg"
								os.system(command)
								if os.path.exists("spot_occupied"):
									os.system("rm -r spot_occupied")
								os.system("mkdir spot_occupied")
								command ="mkdir spot_occupied/"+str(plate)+"="+str(confidence)
								os.system(command)
								lastPlate = plate
								print plate
								print confidence
								print int(time.time())
								print "Spot added"
						else :
							if os.path.exists("spot_occupied"):
								os.system("rm -r spot_occupied")
								print "Spot removed"
								lastPlate = plate
					else :
						print "Same plate identified"
						
				os.system("mv check.jpg previous_img.jpg")
			time.sleep(5)

		except KeyboardInterrupt:
			print "keyboard"
			#os.system("rm -r spot_occupied")
			#os.system("mkdir spot_occupied")
			#os.system("mkdir spot_occupied/ErrorInPI3=Error")
			sys.exit(0)
		
if __name__ == "__main__":
	if os.path.exists("parkedVehicleImages")!=True:
		os.system("mkdir parkedVehicleImages")
	if os.path.exists("spot_occupied"):
		os.system("rm -r spot_occupied")
	if os.path.exists("check.jpg"):
		os.system("rm -r check.jpg")
	if os.path.exists("previous_img.jpg"):
		os.system("rm -r previous_img.jpg")
	main()
	




