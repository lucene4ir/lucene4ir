#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Created on Thu May 24 15:05:08 2018

@author: colin
"""

import sys, os, re

pfile = os.path.abspath(sys.argv[1])

qfile = os.path.abspath(sys.argv[2])
qfn=qfile.split('_')[-1].split('.')[0]

pfilename, pfile_extension = os.path.splitext(pfile)
newpfile=pfilename + '-' + qfn + pfile_extension
print(newpfile)

with open(pfile) as f:
    newfile=''
    for line in f:
        if (re.match('<queryfile>', line, re.I)):
            new_line='<queryFile>%s</queryFile>\n'%(qfile)
            newfile=newfile+new_line
        elif (re.match('<resultfile>', line, re.I)):
            ls=line.split('.')
            new_line=ls[0]+'('+str(qfn)+').'+ls[1]
            newfile=newfile+new_line
        else:
            newfile=newfile+line
    text_file = open(newpfile, "w")
    text_file.write(newfile)
    text_file.close()