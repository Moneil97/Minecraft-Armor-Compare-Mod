package com.examplemod;

public enum TypeModifier {

	protection(.5f, 1.0f);
	
	float upperBound, lowerBound, average;
	
	TypeModifier(float lower, float upper){
		lowerBound = lower;
		upperBound = upper;
		average = (lowerBound + upperBound)/2.0f;
	}
	
	float getUpperValue(){
		return upperBound;
	}
	
	float getLowerValue(){
		return lowerBound;
	}
	
	float getAverageValue(){
		return average;
	}
	
}
