#!/usr/bin/env python

import sys
import os
import math

class Mapper:
    def __init__(self):
        pass
    def __call__(self, key, value):
        value = value.replace(' ', '')
        tempArr = value.split("->")
        user = tempArr[0]
        tempArr1 = tempArr[1].split(",")
        fndWithStatus = tempArr[1].replace(",", "->")
        yield user, (tempArr1[0]+'->'+tempArr1[1])
        yield tempArr1[0], tempArr1[1]


def reducer(key, values):
    ''''yield key, sum(values)'''
    prevUser = None
    tempValArr = []
    criminalRecCount = 0
    nonCriminalRecCount = 0
    for temp in values:
        tempValArr.append(temp)
        tempArr1 = []
        if '->' in temp:
            tempArr1 = temp.split('->')
        if (len(tempArr1) > 1) and (tempArr1[1] == 'yes'):
            criminalRecCount += 1
        elif (len(tempArr1) > 1) and (tempArr1[1] == 'no'):
            nonCriminalRecCount += 1
    
    totalNumberOfFnds = criminalRecCount+nonCriminalRecCount
    if ((nonCriminalRecCount != 0) and (float(float(criminalRecCount)*100/float(totalNumberOfFnds)) >= 50)  and ('yes' not in tempValArr)):
        yield key,  'atrisk'
    else:
        yield key,  'not-atrisk'

if __name__ == "__main__":
    import dumbo
    dumbo.run(Mapper, reducer, combiner=None)
