package org.appseed.tests.metrix;

import org.appseed.metrix.ValueDomain;

import spock.lang.Specification;

class AValueDomain extends Specification {
	
	def 'defined over a class, adds to that class\' instances a read only property returning the domain, only when enabled'(){
		given: 'a domain "distance" created over a class and enabled'
			def distance=new ValueDomain('distance', aClass, true)
		expect: 'every instance of that class has a distance property returning the "distance" domain'
			aValue.distance == distance
		
		when: 'the domain is disabled and the read only property is accessed'
			distance.enabled=false
			aValue.distance
		then: 'a missing property exception is thrown'
			thrown(MissingPropertyException)
			
		where:
			aValue     | aClass
			-1.7       | Number
			 1.7       | Number
			'a string' | String
			 true      | Boolean
			 false     | Boolean
			 467F      | Float
			 12        | Integer
	}
}
