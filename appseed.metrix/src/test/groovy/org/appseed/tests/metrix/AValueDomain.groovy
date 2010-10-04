package org.appseed.tests.metrix;

import org.appseed.metrix.Value;
import org.appseed.metrix.ValueDomain;
import org.appseed.metrix.ValueProvider;

import spock.lang.IgnoreRest;
import spock.lang.Specification;

class AValueDomain extends Specification {
	def valueProvider=Mock(ValueProvider)
	def valueCreatedByProvider=new Value()
	
	def 'defined over a class, with a value provider, and enabled, adds to that class a read only property returning a value created by the provider'(){
		given: 'a domain "distance" is created over a class and enabled'
			def distance=new ValueDomain('distance', aClass, valueProvider)
		
		when: 'when the domain is enabled, and then a property named "distance" on an instance of that class is accessed'
			distance.enabled=true
			def valueReturned=anInstance.distance
		then: 'it will return a value created by the provider using the instance and the domain'
			1 * valueProvider.newValue(anInstance, distance) >> valueCreatedByProvider
			distance.enabled
			valueReturned == valueCreatedByProvider
		
		when: 'the domain is disabled and the read only property is accessed'
			distance.enabled=false
			aValue.distance
		then: 'a missing property exception is thrown and value provider is not called'
			0 * valueProvider.newValue(_, _)
			!distance.enabled
			thrown(MissingPropertyException)
			
		where:
			anInstance | aClass
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
			def distance=new ValueDomain('distance', aClass, valueProvider, true)
		when: 'the read only property is accessed in a non realated class instance'
			anInstance.distance
		then: 'value provider is not called a missing property exception is thrown'
			0 * valueProvider.newValue(_, _)
			thrown(MissingPropertyException)
			
		cleanup:
			distance.enabled=false
		
		where:
			anInstance     | aClass 
			-1.7       | Integer
			 1.7       | String 
			'a string' | Boolean
			 true      | String 
			 false     | Float  
			 467F      | Integer
			 12        | Double	
	}
	
	def 'cannot be defined if another with the same name was defined and enabled earlier'() {
		given: 'a domain "distance" defined and enabled'
			def distance=new ValueDomain('distance', aClass, valueProvider, true)
		
		when: 'another domain "distance" is defined over any other class'
			new ValueDomain('distance', otherClass, valueProvider)
		then: 'an exception is thrown'
			thrown(Throwable)
		
		when: 'but when the former domain is disabled and another domain with the same name is created'
			distance.enabled=false
			def distance2=new ValueDomain('distance', otherClass, valueProvider, true)
			def valueReturned=anInstance.distance
		then: 'it can be used while the other not'
			0 * valueProvider.newValue(anInstance, distance)
			// Seems to be a bug in spock interaction
			// We can only use variables defined in given/setup or where as parameter matchers
			// Should be: 1 * valueProvider.newValue(anInstance, distance2) >> valueCreatedByProvider
			// But it is not working. So the workaround is use custom matcher:
			1 * valueProvider.newValue(anInstance, { it == distance2 }) >> valueCreatedByProvider
			valueReturned == valueCreatedByProvider
			
		cleanup:
			distance2.enabled=false
		
		where:
			anInstance | otherClass  | aClass
			-1.7       | Number      | String
			 1.7       | Number      | Number 
			'a string' | String      | String
			 true      | Boolean     | Boolean
			 false     | Boolean     | Float
			 467F      | Float       | Number
			 12        | Integer     | Boolean
	}
	
	def 'cannot be enabled if another with the same name is enabled'() {
		given: 'two domains called "distance" but not enabled'
			def distance=new ValueDomain('distance', aClass, valueProvider)
			def distance2=new ValueDomain('distance', aClass, valueProvider)
			def anotherValue=new Value()
		
		when: 'both are enabled'
			distance.enabled = true
			def valueReturned=anInstance.distance
			distance2.enabled = true
		then: 'the first one is active and enabled but an exception is thrown for the second one'
			0 * valueProvider.newValue(anInstance, distance2)
			1 * valueProvider.newValue(anInstance, distance) >> valueCreatedByProvider
			valueReturned == valueCreatedByProvider
			!distance2.enabled
			thrown(Throwable)
		
		when: 'the first one is disabled and the other enabled and the property accessed'
			distance.enabled=false
			distance2.enabled=true
			valueReturned=anInstance.distance
		then: 'the second one is active and the first one not'
			1 * valueProvider.newValue(anInstance, distance2) >> anotherValue
			0 * valueProvider.newValue(anInstance, distance)
			valueReturned == anotherValue
			
		cleanup:
			distance2.enabled=false
		
		where:
			anInstance     | aClass 
			-1.7       | Number 
			 1.7       | Number 
			'a string' | String 
			 true      | Boolean
			 false     | Boolean
			 467F      | Float  
			 12        | Integer
	}
	
	def 'defined over a class and other value domain over the same class will not interfere each other'(){
		given: 'a domain "distance" and other "weight", defined over the same class and both enabled'
			def distance=new ValueDomain('distance', aClass, valueProvider, true)
			def weight=new ValueDomain('weight', aClass, valueProvider, true)
			def aWeightValue=new Value()
			def aDistanceValue=new Value()

		when: 'properties defined by both domains are accessed'
			def distanceValueReturned=anInstance.distance
			def weightValueReturned=anInstance.weight;
		then: 'each domain asks its provider for a new value using itself and the instance'
			1 * valueProvider.newValue(anInstance, distance) >> aDistanceValue
			1 * valueProvider.newValue(anInstance, weight) >> aWeightValue
			aWeightValue != aDistanceValue
			distanceValueReturned == aDistanceValue
			weightValueReturned == aWeightValue
			
		
		cleanup:
			distance.enabled=false
			weight.enabled=false
			
		where:
			anInstance     | aClass
			-1.7       | Number
			 1.7       | Number
			'a string' | String
			 true      | Boolean
			 false     | Boolean
			 467F      | Float
			 12        | Integer
	}
}
