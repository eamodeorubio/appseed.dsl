package org.appseed.metrix;

import spock.lang.Specification;

class AValueDomain extends Specification {
	
	def 'is defined over a class and has a name'(){
		when: 'a domain "distance" is created over the class Number and enabled'
			def distance=new ValueDomain(name:'distance', over:Number) 
			distance.enabled=true
		then: 'a proper domain object is built and class Number has a read only property called "distance"'
			distance.name == 'distance'
			distance.over == Number
			1.distance
		
		when: '"distance" domain is disabled'
			distance.enabled=false
			1.distance
		then: 'class Number has not "distance" property any more'
			thrown(MissingPropertyException)
	}
	
	def 'can be defined over any class and each class instance has a read only property returning the domain'(){
		given: 'a domain "distance" created over a class and enabled'
			def distance=new ValueDomain('distance', aClass, true)
		expect: 'every instance of that class has a distance property returning the "distance" domain'
			aValue.distance == distance
		
		cleanup:
			distance.enabled=false
		
		where:
			aValue << [-1.7, 1.7, "a string", true, false, 467F, 12]
			aClass << [Number, Number, String, Boolean, Boolean, Float, Integer]
	}
}
