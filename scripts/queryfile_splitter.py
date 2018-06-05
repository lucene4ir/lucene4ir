#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu May 24 15:05:08 2018

@author: colin
"""

import sys, os

test_path="/Users/colin/Workspace/Lucene-code/lucene4ir/data/ap/ap_bigram.qry"
rel_test_path="data/ap/ap_bigram.qry"


qfile = os.path.abspath(sys.argv[1])
split=os.path.split(qfile)
path=split[0]
filename=split[1]
ext=filename.split(".")[1]
name=filename.split(".")[0]

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
           nf=path+"/"+name+"_"+str(j)+"."+ext
           j+=1
           print(nf)
           text_file = open(nf, "w")
           text_file.write(new_file_str)
           text_file.close()
           i=1
           new_file_str=""

    if new_file_str!="":
        nf=path+"/"+name+"_"+str(j)+"."+ext
        print(nf)
        text_file = open(nf, "w")
        text_file.write(new_file_str)
        text_file.close()
#    Catch last lines of file when no.Q is not divisible by 10000    
