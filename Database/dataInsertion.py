import pandas as pd
import pyodbc

# import cities data
data = pd.read_csv ('gr.csv',
                    usecols=[0,1,2],
                    sep=',',
                    index_col=False)
# drop duplicates
data = data.drop_duplicates(subset=['lng','lat'])
data = data.drop_duplicates(subset='city')

# get credentials for connection
filename = "Constants.java"
with open(filename) as f:
    content = f.read().splitlines()
for line in content:
    if 'HOST' in line:
        HOST = line[line.index('HOST = "')+8 : len(line)-2]
    elif 'PORT' in line:
        PORT = line[line.index('PORT = "')+8 : len(line)-2]
    elif 'DATABASE' in line:
        DATABASE = line[line.index('DATABASE = "')+12 : len(line)-2]
    elif 'USER' in line:
        USERNAME = line[line.index('USER = "')+8 : len(line)-2]
    elif 'PASSWORD' in line:
        PASSWORD = line[line.index('PASSWORD = "')+12 : len(line)-2]

# create connection
conn = pyodbc.connect('Driver={SQL Server};'
                      'Server=%s;'
                      'Database=%s;'
                      'UID=%s;'
                      'PWD=%s;' % (HOST,DATABASE,USERNAME,PASSWORD))
cursor = conn.cursor()

# insert cities to database
for row in data.itertuples():
    cursor.execute('''INSERT INTO Location (name, longitude, latitude) VALUES (?,?,?)''',
                   row.city, row.lng, row.lat)
conn.commit()
conn.close()