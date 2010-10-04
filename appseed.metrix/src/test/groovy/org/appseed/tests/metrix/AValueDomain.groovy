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
	
	def 'defined over a class will not affect other classes not extending that one'(){
		given: 'a domain "distance" created over a class and enabled'
			def distance=new ValueDomain('distance', aClass, true)
		when: 'the read only property is accessed in a non realated class instance'
			aValue.distance
		then: 'a missing property exception is thrown'
			aClassInstance.distance == distance
			thrown(MissingPropertyException)
			
		cleanup:
			distance.enabled=false
		
		where:
			aValue     | aClass  | aClassInstance
			-1.7       | Integer | 1
			 1.7       | String  | 'pepe'
			'a string' | Boolean | true
			 true      | String  | 'juan'
			 false     | Float   | 45F
			 467F      | Integer | 33
			 12        | Double	 | 34D
	}
	
	def 'defined over a class and other value domain over the same class will not interfere each other'(){
		given: 'a domain "distance" and other "weight", defined over the same class and both enabled'
			def distance=new ValueDomain('distance', aClass, true)
			def weight=new ValueDomain('weight', aClass, true)
		expect: 'every instance of that class has a distance property returning the "distance" domain and a weight property returning the "weight" domain'
			weight != distance
			aValue.distance == distance
			aValue.weight == weight
		
		cleanup:
			distance.enabled=false
			weight.enabled=false
			
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
