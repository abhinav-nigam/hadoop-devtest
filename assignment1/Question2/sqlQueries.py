#!/usr/bin/env python

import sys
import os
import math

class Mapper:
    def __init__(self):
        pass
    def __call__(self, key, value):
        tempArr = value.split(',')
        name = tempArr[0]
        age = tempArr[1]
        sex = tempArr[2]
        occupation = tempArr[3]
        incomeLevel = tempArr[4]
        if(sex == 'Male'):
            yield (value+',query1'), 1    
        yield (name+","+occupation+',query2'), 1
        yield (incomeLevel+',query3'), 1


def reducer(key, values):
    '''tempValArr = [int(temp) for temp in values]'''
    tempArr = key.split(",")
    if(tempArr[-1] == 'query3'):
        yield (tempArr[-1],tempArr[0:(len(tempArr)-1)]), sum(values)
    else:
        yield (tempArr[-1],tempArr[0:(len(tempArr)-1)]), ""

if __name__ == "__main__":
    import dumbo
    dumbo.run(Mapper, reducer, combiner=None)
