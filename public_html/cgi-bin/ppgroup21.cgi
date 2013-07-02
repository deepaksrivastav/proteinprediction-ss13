#!/usr/bin/python
import cgi, os
import subprocess
import cgitb

cgitb.enable()

if os.environ['REQUEST_METHOD'] == 'GET':
    print """\
Content-Type: text/html\n
<html>
<body>
<form enctype="multipart/form-data" action="ppgroup21.cgi" method="POST">
<h2>Team 21 Predictor</h2>
<p>File: <input type=\"file\" name=\"upload\"></p>
<p><input type=\"submit\" value=\"Predict\"></p>
</form>
</body></html>
"""

else:
    form = cgi.FieldStorage()

    #fileitem from the form
    fileitem = form['upload']
    if fileitem.filename:

       # extract file name and check if the file has .arff extension
       # if not append the .arff extension - this is to ensure that DataSource
       # API of Weka does not throw an error
       fn = os.path.basename(fileitem.filename)
       if not (fn.endswith('.arff')):
            fn = fn + '.arff'
       outFile = open(fn, 'w')
       while 1:
          chunk = fileitem.file.read(100000)
          if not chunk:
             break
          outFile.write(chunk)
       outFile.close()

       # run our predictor on the file
       command = 'java -jar team21.jar %s' % fn
       proc = subprocess.Popen(command,stdout=subprocess.PIPE,shell=True)
       (message, err) = proc.communicate()

       # remove the file as it is no longer necessary
       os.remove(fn)

    else:
       # error in case the file was not uploaded properly
       message = 'No file was uploaded'

    # flus the output
    print """\
Content-Type: text/plain\n
%s
""" % (message)
