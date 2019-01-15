import os
import time


occupancy =1
previous_occupancy = 1


result = os.system("clear")
print "___"
print result
print "___"

result = os.system("iffig")
print "___"
print result
print "___"

i = os.path.exists("srsc")
print i




'''
if occupancy & (occupancy!=previous_occupancy):
	
	print "exists"
else :
	print "no"


def process_img():
	return 1


def main():
	while True:
		exists = os.path.isfile('arun.jpg')
		if exists:
			# Store configuration file values
			print "exists"
			occupancy = process_img()			
			if occupancy & (occupancy!=previous_occupancy):
				previous_occupancy = occupancy
				os.system("mkdir spot_occupied")
			else :
				os.system("mkdir spot_occupied")
			
		else:
			# Keep presets
			print "no"
		time.sleep(5)

if __name__ == "__main__":
    main()

'''
