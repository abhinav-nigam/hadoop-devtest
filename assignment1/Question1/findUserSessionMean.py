#!/usr/bin/env python

import sys
import os
import math

class Mapper:
    def __init__(self):
        pass
    def __call__(self, key, value):
        tempArr = value.split("\t")
        userId = tempArr[0]
        sessionLen = tempArr[1]
        yield userId, int(sessionLen)


def reducer(key, values):
    ''''yield key, sum(values)'''
    tempValArr = [int(temp) for temp in values]
    tempValArr.sort()
    tempLen = len(tempValArr)
    middleOfArr = int(math.ceil(float(float(tempLen)/2))) - 1;
    if (tempLen % 2) == 1 :
        yield key, tempValArr [middleOfArr]
    else :
        yield key, (((tempValArr [middleOfArr]  + tempValArr [middleOfArr+1])/2))

if __name__ == "__main__":
    import dumbo
    dumbo.run(Mapper, reducer, combiner=None)
