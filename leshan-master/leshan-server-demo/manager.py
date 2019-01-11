#!/usr/bin/python
 
import sqlite3
from sqlite3 import Error
import time 
import sys
 
def create_connection(db_file):
    """ create a database connection to the SQLite database
        specified by the db_file
    :param db_file: database file
    :return: Connection object or None
    """
    try:
        conn = sqlite3.connect(db_file)
        return conn
    except Error as e:
        print(e)
 
    return None
 
 
def select_all_tasks(conn):
    """
    Query all rows in the tasks table
    :param conn: the Connection object
    :return:
    """
    cur = conn.cursor()
    cur.execute("SELECT * FROM OVERVIEW")
 
    rows = cur.fetchall()
 
    for row in rows:
        print(row)
 
 
def select_entry_by_id(conn, clientID):
    """
    Query tasks by priority
    :param conn: the Connection object
    :param priority:
    :return:
    """
    cur = conn.cursor()
    cur.execute("SELECT * FROM OVERVIEW WHERE PIID=?", (clientID,))
 
    rows = cur.fetchall()
 
    for row in rows:
        print(row)

def select_id_occu_carID(conn):
    """
    Query tasks by priority
    :param conn: the Connection object
    :param priority:
    :return:
    """
    cur = conn.cursor()
    cur.execute("SELECT PIID,STATE,CARNUMBER FROM OVERVIEW")
 
    rows = cur.fetchall()
 
    for row in rows:
        #print(row)
	#sys.stdout.write("\rDoing thing %row" % row)
	sys.stdout.flush()
 
 
def main():
    database = "IoTParking.db"
    i = 0	
    # create a database connection
    conn = create_connection(database)
    with conn:
	while True:
		i = i+1
		sys.stdout.write("\rDoing thing %i" % i)
		sys.stdout.flush()
		#print(str(i)+". Manager View")
		select_id_occu_carID(conn)
		time.sleep(5);

 
 
if __name__ == '__main__':
    main()
