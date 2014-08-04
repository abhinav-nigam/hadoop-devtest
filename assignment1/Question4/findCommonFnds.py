#!/usr/bin/env python

import sys
import os
import math

class Mapper:
    def __init__(self):
        pass
    def __call__(self, key, value):
        tempArr = value.replace(' ','').split('->')
        tempArr1 = tempArr[1].replace(' ','').split(',')
	for x in tempArr1:
		combinationsTempArr = [tempArr[0],x]
		combinationsTempArr.sort()
		yield (','.join(combinationsTempArr)), tempArr[1].replace((x+','),'').replace((','+x),'').replace(' ','')


def reducer(key, values):
    tempValArr = []
    commonFnds = []
    for temp in values:
        tempArr = temp.split(",")
        for temp1 in tempArr:
            if(temp1 in tempValArr):
                commonFnds.append(temp1)
            else:    
                tempValArr.append(temp1)
    yield key, commonFnds

if __name__ == "__main__":
    import dumbo
    dumbo.run(Mapper, reducer, combiner=None)
