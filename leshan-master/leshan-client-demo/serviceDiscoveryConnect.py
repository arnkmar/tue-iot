import os

os.system("rm -r  avahiout.txt")
os.system("avahi-browse -rtp  _coap._udp > avahiout.txt")

os.system("sed '/^+/ d' avahiout.txt > avahiout_temp.txt && mv avahiout_temp.txt avahiout.txt ")

while True:
	with open('avahiout.txt') as f:
	    line = f.readline()
	
	if line=='':
		print "No service was discovered. Try Again."
		break

	else :	
		os.system("tail -n +2 avahiout.txt > avahiout_temp.txt && mv avahiout_temp.txt avahiout.txt")
		val = line.split(';')
		if val[2]=='IPv4':
			ip = val[7]
			print "\nConnecting to server that was first listed during discovery process."
			print "\nConnecting to Service Name = "+ val[3] +" at ServerIP = "+ip+"\n\n\n"
			os.system("java -jar runnable_1.jar -u "+ip+" -n parking-1")
			break


print "Process ends now."
