
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Created on Thu May 24 15:05:08 2018

@author: colin
"""

import sys, os, re

pfile = os.path.abspath(sys.argv[1])

qfile = os.path.abspath(sys.argv[2])
qfn=qfile.split('-')[-1].split('.')[0]

pfilename, pfile_extension = os.path.splitext(pfile)
newpfile=pfilename + '-' + qfn + pfile_extension


with open(pfile) as f:
    newfile=''
    for line in f:
        if (re.match('<queryfile>', line, re.I)):
            new_line='<queryFile>%s</queryFile>\n'%(qfile)
            newfile=newfile+new_line
        elif (re.match('<resultfile>', line, re.I)):
            start = line.index( '<resultFile>' ) + len( '<resultFile>' )
            end = line.index( '</resultFile>', start )
            rfile=line[start:end]
            rfilename, rfile_extension = os.path.splitext(rfile)
            new_line='<resultFile>%s-%s%s</resultFile>\n'%(rfilename,str(qfn),rfile_extension)
            newfile=newfile+new_line
        else:
            newfile=newfile+line
    text_file = open(newpfile, "w")
    text_file.write(newfile)
    text_file.close()
    print(newpfile)
