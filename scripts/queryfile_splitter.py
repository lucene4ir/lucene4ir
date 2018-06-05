#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu May 24 15:05:08 2018

@author: colin
"""

import sys, os

qfile = os.path.abspath(sys.argv[1])
name, ext = os.path.splitext(qfile)

with open(qfile) as f:
    i=1
    j=1
    new_file_str=""
    for line in f:
       if i < 10000: 
           new_file_str+=line
           i+=1
       elif i==10000: 
           new_file_str+=line
           nf=name+"-"+str(j)+ext
           j+=1
           text_file = open(nf, "w")
           text_file.write(new_file_str)
           text_file.close()
           print(nf)
           i=1
           new_file_str=""

    if new_file_str!="":
        nf=name+"-"+str(j)+ext
        text_file = open(nf, "w")
        text_file.write(new_file_str)
        text_file.close()
        print(nf)
#    Catch last lines of file when no.Q is not divisible by 10000    
